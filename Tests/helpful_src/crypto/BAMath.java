package helpful.crypto;
import java.util.LinkedList;
class BAMath{
	
	/* General Usage Notes:
	 * The byte arrays in use assume index 0 has the least significant digit and the array expands "leftward" as the size increases so index 1 would contain the bits representing 2^16-1
	 * If the bytes given has index 0 as the most significant bits, you sould flip the order before performing any operations
	 * Each function should return a new list so the old one can have math done on it without saving the result
	 */
	 
	/* TODO:
	 * Negative collection tests 
	 * Bitwise tests
	 */
	
	//Doing the actual addition math on the data
	public static byte[] add(byte[] a, byte[] b){
		/*if (a == null || b == null) throw new NullPointerException("Both arrays must actually exist");
		if (a.length == 0 || b.length == 0) throw new IllegalArgumentException("Both arrays need to at least have 1 byte each");
		
		byte[] send = new byte[Math.max(a.length, b.length) + 1];
		boolean[] carry = new boolean[1];
		
		for (int i=0;i<send.length;i++){
			if (i < a.length && i < b.length){ //we can add from both arrays
				send[i] = (byte)(a[i] ^ b[i] ^ genCarries(a[i],b[i],carry));
			}
			else if (i < a.length){ //a's length is more than b's
				send[i] = (byte)(a[i] ^ genCarries(a[i],(byte)0,carry));
			}
			else if (i < b.length){ //b's length is more than a's
				send[i] = (byte)(b[i] ^ genCarries(b[i],(byte)0,carry));
			}
			else if (i >= a.length && i >= b.length && carry[0]){ //out of bounds for A and B but we still have the super carry
				send[i]=1; //guarenteed to be 0 before we reach it, super carry is true, guarenteed to become 1 and only 1
				carry[0] = false; //doesn't carry on any further
			}
			//System.out.println("DEBUG: " + send[i] + ", " + carry[0]);
		}
		//return send;*/
		return trim(add(a, b, (isNegative(a) ^ isNegative(b)) ? 0:1));
	}
	public static byte[] add(byte[] a, byte b){
		return add(a, new byte[] {b});
	}
	protected static byte[] add(byte[] a, byte[] b, int bufferSpace){ //adds the two lists and returns a list with the maximum length of the 2 inputs + bufferSpace as its length
		if (a == null || b == null) throw new NullPointerException("Both arrays must actually exist");
		if (a.length == 0 || b.length == 0) throw new IllegalArgumentException("Both arrays need to at least have 1 byte each");
		
		byte[] send = new byte[Math.max(a.length, b.length) + bufferSpace];
		boolean[] carry = new boolean[1];
		
		for (int i=0;i<send.length;i++){
			if (i < a.length && i < b.length){ //we can add from both arrays
				send[i] = (byte)(a[i] ^ b[i] ^ genCarries(a[i],b[i],carry));
			}
			else if (i < a.length){ //a's length is more than b's
				send[i] = (byte)(a[i] ^ genCarries(a[i],(byte)0,carry));
			}
			else if (i < b.length){ //b's length is more than a's
				send[i] = (byte)(b[i] ^ genCarries(b[i],(byte)0,carry));
			}
			else if (i >= a.length && i >= b.length && carry[0]){ //out of bounds for A and B but we still have the super carry
				send[i]=1; //guarenteed to be 0 before we reach it, super carry is true, guarenteed to become 1 and only 1
				carry[0] = false; //doesn't carry on any further
			}
			//System.out.println("DEBUG: " + send[i] + ", " + carry[0]);
		}
		//return send;
		return send;
	}
	protected static byte[] addR(byte[] a, byte[] b){
		if (a == null || b == null) throw new NullPointerException("Both arrays must actually exist");
		if (a.length == 0 || b.length == 0) throw new IllegalArgumentException("Both arrays need to at least have 1 byte each");
		boolean[] carry = new boolean[1];
		byte carries;
		
		for (int i=0;i<a.length;i++){
			if (i<b.length){ // can preform the addition
				carries = genCarries(a[i], b[i], carry);
				a[i] = (byte)(a[i] ^ b[i] ^ carries);
			}
			else if (carry[0]){
				carries = genCarries(a[i], (byte)0, carry);
				a[i] = (byte)(a[i]^carries);
				carry[0] = false;
			}
		}
		
		return a;
	}
	
	//Doing the subtratction math through two's complement and smaller addition
	public static byte[] subtract(byte[] a, byte[] b){
		if (a == null || b == null) throw new NullPointerException("Both arrays must actually exist");
		if (a.length == 0 || b.length == 0) throw new IllegalArgumentException("Both arrays need to at least have 1 byte each");
		
		byte[] send;
		
		//Ensure the two arrays are the same size so we don't need to worry about the super carry
		//if (a.length < b.length) a = expand(a, b.length);
		//else if (a.length > b.length) b = expand(b, a.length);
		boolean flipBack = false;
		if (a[a.length-1] == 0 && a.length > 1){ a = negate(a); flipBack = true; }
		else b = negate(b); //turn b negative according to the 2's complement system
		//System.out.print("\n Addition Debug: " + Debug.toString(a) + ", " + Debug.toString(b) + " ---- ");
		send = add(a, b, 0); //add positive and negative for subtraction
		if (flipBack) send = negate(send); //un-does the inital flipping meaning its a-b, without it, the equation becomes b-a
		
		//System.out.println("\n\n\n\nDebug: " + Debug.toString(a) + ", B: " + Debug.toString(b) + ", Send: " + Debug.toString(send) + "\n");
		
		return trim(send); //uncertain of final size, can trim when done to reduce array size
	}
	protected static byte[] subtractR(byte[] a, byte[] b){
		if (a == null || b == null) throw new NullPointerException("Both arrays must actually exist");
		if (a.length == 0 || b.length == 0) throw new IllegalArgumentException("Both arrays need to at least have 1 byte each");
		
		boolean flipBack = false;
		if (a[a.length-1] == 0 && a.length > 1){ a = negateR(a); flipBack=true; }
		else b = negate(b);
		
		addR(a, b);
		
		if (flipBack) a = negateR(a);
		
		return a;
	}
	
	//Doing the actual multiplication on the data
	public static byte[] multiply(byte[] a, byte[] b){ //O(n*m)
		if (a == null || b == null) throw new NullPointerException("Both arrays must actually exist");
		if (a.length == 0 || b.length == 0) throw new IllegalArgumentException("Both arrays need to at least have 1 byte each");
		
		boolean isSignNegative = isNegative(a) ^ isNegative(b);
		if (isNegative(a)) a = negate(a);
		if (isNegative(b)) b = negate(b);
		
		byte[] send = new byte[a.length+b.length];
		byte[] lShiftA = new byte[a.length + getGreatestBit(b)/8+1];
		System.arraycopy(a, 0, lShiftA, 0, a.length);
		for (int i=0;i<b.length*8;i++){ //(O(m)
			
			if (!getBit(b, i)){
				lShiftR(lShiftA, 1);
				continue;
			}
			//is it faster to have a running to-add list of bits or just generate it each cycle?
			//send = add(send, lShift(a, i)); //repeated addition as there is no set size to create optimizations with
			send = add(send, lShiftA); //O(n)
			lShiftR(lShiftA, 1);
		}
		if (isSignNegative) negateR(send);
		return trim(send);
	}
	
	//Doing the actual division on the data
	public static byte[] divide(byte[] a, byte[] b){ //divides a by b - Integer division, without remainder
		return divide(a, b, new byte[0]);
	}
	protected static byte[] divide(byte[] a, byte[] b, byte[] remainder){ //O(n^2)
		if (a == null || b == null || remainder == null) throw new NullPointerException("All arrays must actually exist");
		if (a.length == 0 || b.length == 0) throw new IllegalArgumentException("Both arrays need to at least have 1 byte each");
		
		boolean isSignNegative = isNegative(a) ^ isNegative(b);
		if (isNegative(a)) a = negate(a);
		if (isNegative(b)) b = negate(b);
		
		byte[] send = new byte[a.length];
		int pos = getGreatestBit(a) - getGreatestBit(b); //aligns the two left most bits to start off
		if (pos < 0){ //guarenteed to return 'a' as the remainder as 'b' is too big
			if (remainder.length > 0) System.arraycopy(a, 0, remainder, 0, Math.min(a.length, remainder.length));
			return send; //0
		}
		byte[] rShiftB = lShift(b, pos);
		byte[] runningA = new byte[a.length]; //to not overwrite the "A" array
		System.arraycopy(a, 0, runningA, 0, a.length);
		
		while (greater(runningA, b) != b && pos >= 0){ //while we can still remove the divisor from the dividend
			
			if (greater(runningA, rShiftB) == rShiftB){ //divisor is too big, shrink it
				pos--;
				rShiftB=rShift(rShiftB, 1);
				continue;
			}
			//we are small enough, subtract from the running total, add to send, remove from send and go down a bit
			runningA = subtract(runningA, rShiftB);
			setBit(send, pos, true);
			rShiftB=rShift(rShiftB, 1);
			pos--;
		}
		if (remainder.length > 0) System.arraycopy(runningA, 0, remainder, 0, Math.min(runningA.length, remainder.length)); //save the remainder where the invoker can access it
		
		if (isSignNegative) negateR(send);
		
		return trim(send);
	}
	protected static byte[] divideR(byte[] a, byte[] b, byte[] remainder){ //returns the integer of the quotient
		byte[] send = new byte[a.length];
		
		boolean isSignNegative = isNegative(a) ^ isNegative(b);
		if (isNegative(a)) a = negateR(a);
		if (isNegative(b)) b = negate(b);
		
		int pos = getGreatestBit(a) - getGreatestBit(b);
		if (pos < 0){//guarenteed 0 will be returned - 'a' is smaller than 'b'
			if (remainder.length > 0) System.arraycopy(a, 0, remainder, 0, Math.min(a.length, remainder.length));
			for (int i=0;i<a.length;i++){a[i] = 0;}
			return a; 
		}
		byte[] rShiftB = lShift(b, pos);
		
		while (greater(a, b) != b && pos >= 0){
			if (greater(a, rShiftB) == rShiftB){
				rShiftR(rShiftB, 1);
				pos--;
				continue;
			}
			subtractR(a, rShiftB);
			setBit(send, pos, true);
			rShiftR(rShiftB, 1);
			pos--;
		}
		//Swap the variables so the pointer passed in points to the list of the quotient, and remainder contains the remainder
		if (remainder.length > 0) System.arraycopy(a, 0, remainder, 0, Math.min(a.length, remainder.length));
		System.arraycopy(send, 0, a, 0, a.length);
		
		if (isSignNegative) negateR(a);
		
		return a;
	}
	
	//Doing the acutal modulo on the data
	public static byte[] modulo(byte[] a, byte[] b){ //O(n*m)
		if (a == null || b == null) throw new NullPointerException("Both arrays must actually exist");
		if (a.length == 0 || b.length == 0) throw new IllegalArgumentException("Both arrays need to at least have 1 byte each");
		
		byte[] send = new byte[a.length];
		System.arraycopy(a, 0, send, 0, a.length);
		int pos = getGreatestBit(a) - getGreatestBit(b);
		if (pos < 0)return send;
		byte[] rShiftB = lShift(b, pos);
		
		while (greater(send, b) != b && pos >= 0){ //O(2n)
			if (greater(send, rShiftB) == rShiftB){
				rShiftR(rShiftB, 1);
				pos--;
				continue;
			}
			subtractR(send, rShiftB); //O(n)
			rShiftR(rShiftB, 1);
			pos--;
		}
		return trim(send);
	}
	protected static byte[] moduloR(byte[] a, byte[] b){
		if (a == null || b == null) throw new NullPointerException("Both arrays must actually exist");
		if (a.length == 0 || b.length == 0) throw new IllegalArgumentException("Both arrays need to at least have 1 byte each");
		
		int pos = getGreatestBit(a) - getGreatestBit(b);
		if (pos < 0)return a;
		byte[] rShiftB = lShift(b, pos);
		
		while (greater(a, b) != b && pos >= 0){
			if (greater(a, rShiftB) == rShiftB){
				rShiftR(rShiftB, 1);
				pos--;
				continue;
			}
			subtractR(a, rShiftB);
			rShiftR(rShiftB, 1);
			pos--;
		}
		return a;
	}
	
	//Modulo multiplication - useful for cryptography
	public static byte[] moduloMult(byte[] a, byte[] mult, byte[] mod){ //O(n^2)
		byte[] send = multiply(a, mult); //O(n*m)
		return modulo(send, mod);
	}
	
	//Shifts the entire array to the left or right n number of bits
	public static byte[] rShift(byte[] a, int n){ //drops bits that get pushed into the rightward edge
		if (a == null) throw new NullPointerException("The array must actually exist");
		if (n < 0) throw new IllegalArgumentException("Bit shift amount must be positive, got " + n);
		if (a.length == 0) return new byte[0]; //new array - guaranteed empty
		if (n == 0){ //no movement happens
			byte[] send = new byte[a.length];
			System.arraycopy(a, 0, send, 0, a.length);
			return send; //returns new list as required
		}
		
		byte[] send = new byte[a.length];
		byte lastBit = 0, temp = 0, mask = (byte)(~(0xff << Math.min(n&0b111, 8))); //mask gets initalised with 1s on the right depending on the shift size with a maximum of being 0xff
		System.arraycopy(a, 0, send, 0, a.length);
		if ((n & 0b00000111) != 0){ //not divisible by 8, deal with the excess
			for (int i=a.length-1;i>=0;i--){
				//steal the bits being shifted off with mask and temp
				temp = (byte)((a[i]&mask) << (8-Math.min(n&0b111,8))); //steals bits and moves them to the left most position
				
				//shift the actual bits into new position
				send[i] = (byte)(((~mask)&a[i]&0xff) >>> Math.min(n&0b111,8));
				
				//add in the previous bits into this one
				send[i] |= lastBit;
				
				//shift the temp into last bit for next cycle
				lastBit = temp;
			}
		}
		n>>>=3; //divide by 8
		if (n == 0) return trim(send); //already dealt with the shift, can return
		
		//dealing with entire byte movements
		lastBit = 0;
		temp = 0;
		for (int j=0;j<n;j++){
			for (int i=a.length-1-j;i>=0;i -= n){
				//Move into temporary byte
				temp = send[i];//(byte)((a[i]&mask) << (8-Math.min(n,8))); //steals bits and moves them to the left most position
				
				//Move the previous byte into here
				send[i] = lastBit;
				
				//Move the temp into position for the next array
				lastBit = temp;
			}
		}
		return trim(send);
	}
	protected static byte[] rShiftR(byte[] a, int n){ //drops bits that get pushed into the rightward edge
		if (a == null) throw new NullPointerException("The array must actually exist");
		if (n < 0) throw new IllegalArgumentException("Bit shift amount must be positive, got " + n);
		if (a.length == 0) return new byte[0]; //new array - guaranteed empty
		if (n == 0) return a;//no movement happens
		
		byte lastBit = 0, temp = 0, mask = (byte)(~(0xff << Math.min(n&0b111, 8))); //mask gets initalised with 1s on the right depending on the shift size with a maximum of being 0xff
		if ((n & 0b00000111) != 0){ //not divisible by 8, deal with the excess
			for (int i=a.length-1;i>=0;i--){
				//steal the bits being shifted off with mask and temp
				temp = (byte)((a[i]&mask) << (8-Math.min(n&0b111,8))); //steals bits and moves them to the left most position
				
				//shift the actual bits into new position
				a[i] = (byte)(((~mask)&a[i]&0xff) >>> Math.min(n&0b111,8));
				
				//add in the previous bits into this one
				a[i] |= lastBit;
				
				//shift the temp into last bit for next cycle
				lastBit = temp;
			}
		}
		n>>>=3; //divide by 8
		if (n == 0) return a; //already dealt with the shift, can return
		
		//dealing with entire byte movements
		lastBit = 0;
		temp = 0;
		for (int j=0;j<n;j++){
			for (int i=a.length-1-j;i>=0;i -= n){
				//Move into temporary byte
				temp = a[i];//(byte)((a[i]&mask) << (8-Math.min(n,8))); //steals bits and moves them to the left most position
				
				//Move the previous byte into here
				a[i] = lastBit;
				
				//Move the temp into position for the next array
				lastBit = temp;
			}
		}
		return a;
	}
	public static byte[] lShift(byte[] a, int n){
		if (a == null) throw new NullPointerException("The array must actually exist");
		if (n < 0) throw new IllegalArgumentException("Bit shift amount must be positive, got " + n);
		if (a.length == 0) return new byte[0]; //new array - guaranteed empty
		if (n == 0){ //no movement happens
			byte[] send = new byte[a.length];
			System.arraycopy(a, 0, send, 0, a.length);
			return send; //returns new list as required
		}
		
		byte[] send = new byte[((getGreatestBit(a)+n)>>>3)+1]; //will shrink the list down
		//System.out.println("\n"+getGreatestBit(a) + ", " + n);
		byte lastBit = 0, temp = 0, mask = (byte)((0xff << (8-(n&0b111)))); //mask gets initalised with 1s on the right depending on the shift size with a maximum of being 0xff
		System.arraycopy(a, 0, send, 0, a.length);
		if ((n & 0b00000111) != 0){ //not divisible by 8, deal with the excess
			for (int i=0;i<send.length;i++){
				//System.out.println("\nDebug Cycle " + i + ": a[i]: " + (i<a.length?a[i]:"-") + ", send[i]: " + send[i] + ", mask: " + mask + ", n: " + n + ", temp: " + temp + ", lastBit: " + lastBit);
				//steal the bits being shifted off with mask and temp
				if (i<a.length) temp = (byte)((a[i]&mask&0xff) >>> (8-(n&0b111))); //steals bits and moves them to the right most position
				else temp = 0;
				
				//shift the actual bits into new position
				if (i<a.length)send[i] = (byte)(((~mask)&a[i]&0xff) << (n&0b111));
				
				//add in the previous bits into this one
				send[i] |= lastBit;
				
				//shift the temp into last bit for next cycle
				lastBit = temp;
				//System.out.println("Debug Cycle " + i + ": a[i]: " + (i<a.length?a[i]:"-") + ", send[i]: " + send[i] + ", mask: " + mask + ", n: " + n + ", temp: " + temp + ", lastBit: " + lastBit);
			}
		}
		n>>>=3; //divide by 8
		if (n == 0) return trim(send); //already dealt with the shift, can return
		
		//dealing with entire byte movements
		lastBit = 0;
		temp = 0;
		for (int j=0;j<n;j++){
			for (int i=j;i<send.length;i += n){
				//Move into temporary byte
				temp = send[i];//(byte)((a[i]&mask) << (8-Math.min(n,8))); //steals bits and moves them to the left most position
				
				//Move the previous byte into here
				send[i] = lastBit;
				
				//Move the temp into position for the next array
				lastBit = temp;
			}
		}
		return trim(send);
	}
	protected static byte[] lShiftR(byte[] a, int n){ //drops bits off the leftward edge
		if (a == null) throw new NullPointerException("The array must actually exist");
		if (n < 0) throw new IllegalArgumentException("Bit shift amount must be positive, got " + n);
		if (a.length == 0) return new byte[0]; //new array - guaranteed empty
		if (n == 0) return a; //no movement happens
		
		//byte[] send = new byte[((getGreatestBit(a)+n)>>>3)+1]; //will shrink the list down
		byte lastBit = 0, temp = 0, mask = (byte)((0xff << (8-(n&0b111)))); //mask gets initalised with 1s on the right depending on the shift size with a maximum of being 0xff
		//System.arraycopy(a, 0, send, 0, a.length);
		if ((n & 0b00000111) != 0){ //not divisible by 8, deal with the excess
			for (int i=0;i<a.length;i++){
				//steal the bits being shifted off with mask and temp
				temp = (byte)((a[i]&mask&0xff) >>> (8-(n&0b111))); //steals bits and moves them to the right most position
				
				//shift the actual bits into new position
				a[i] = (byte)(((~mask)&a[i]&0xff) << (n&0b111));
				
				//add in the previous bits into this one
				a[i] |= lastBit;
				
				//shift the temp into last bit for next cycle
				lastBit = temp;
			}
		}
		n>>>=3; //divide by 8
		if (n == 0) return a; //already dealt with the shift, can return
		
		//dealing with entire byte movements
		lastBit = 0;
		temp = 0;
		for (int j=0;j<n;j++){
			for (int i=j;i<a.length;i += n){
				//Move into temporary byte
				temp = a[i];//(byte)((a[i]&mask) << (8-Math.min(n,8))); //steals bits and moves them to the left most position
				
				//Move the previous byte into here
				a[i] = lastBit;
				
				//Move the temp into position for the next array
				lastBit = temp;
			}
		}
		return a;
	}
	
	//Comparison operators - All returns are without array replacements, returns pointer passed in that is valid to its function
	public static byte[] greater(byte[] a, byte[] b){ //O(2n) //Returns the list pointer that is bigger, think of it as a maximum function
		int pos = getGreatestBit(xor(a, b)); //O(2n)
		//System.out.print("\nDebug: " + pos + ", " + Debug.toString(xor(workA, workB)));
		if (!getBit(a, 0) && !getBit(b, 0) && pos == 0) return null; //didnt find any flipped bits, both are equal
		if (getBit(a, pos) && pos != ((a.length*8)-1)) return a; // if a has the bit and it isn't the negative bit
		return b;
	}
	public static byte[] lesser(byte[] a, byte[] b){ //piggy backs on the greater function, just returning the opposite
		byte[] out = greater(a, b);
		if (out == null) return null;
		if (out == a) return b;
		return a; //out == b
	}
	public static boolean equals(byte[] a, byte[] b){
		byte[] out = greater(a, b);
		return out == null;
	}
	
	//Bitwise operations
	public static byte[] xor(byte[] a, byte[] b){ //O(n) - TODO: Test
		byte[] send = new byte[Math.max(a.length, b.length)];
		for (int i=0;i<send.length;i++){
			if (a.length <= i) send[i] = b[i]; //n^0=n
			else if (b.length <= i) send[i] = a[i];
			else send[i] = (byte)(a[i] ^ b[i]);
			
		}
		return send;
	}
	protected static byte[] xorR(byte[] a, byte[] b){ //O(n) //overwrites the "A" array - TODO: Test
		for (int i=0;i<Math.min(a.length, b.length);i++){
			a[i] ^= b[i];
		}
		return a;
	}
	
	//Helping functions for the actual math
	
	//Flips bits 
	public static byte[] flip(byte[] a){
		byte[] send = new byte[a.length];
		for (int i=0;i<a.length;i++){
			send[i] = (byte)~a[i];
		}
		return send;
	}
	protected static byte[] flipR(byte[] a){ //flips the bits in place - no new list created
		for (int i=0;i<a.length;i++){
			a[i] = (byte)~a[i];
		}
		return a;
	}
	
	//Negative collection'
	public static byte[] negate(byte[] a){ //O(2n) //negates the value passed in - TODO: Test
		return add(flip(a), new byte[] {(byte)1}, 0);
	}
	public static byte[] negateR(byte[] a){
		return addR(flipR(a), new byte[] {(byte)1});
	}
	public static boolean isNegative(byte[] a){ //O(1) //Returns true if the value passed in is negative according to the 2's complement system - TODO: Test
		return ((a[a.length-1] >> 7) & 0b1) == 1; //returns true if leading bit is "On"
	}
	
	//Fills the byte array with leading 0s or 1s depending on the most significant bit
	protected static byte[] expand(byte[] a, int size){
		if (a==null) throw new NullPointerException("Array must exist");
		if (size < a.length) throw new IllegalArgumentException("The array's length must be longer than the desired output length");
		byte[] send = new byte[size];
		if (a.length == 0) return send; //guaranteed to be all 0s
		System.arraycopy(a, 0, send, 0, a.length);
		if (a.length == size) return send; //Already have the desired length, but must return a new list
		byte expansion = (byte)(a[a.length-1] >> 7); //either 1s or 0s expand to fill all the bits depending on the leading bit
		for (int i=a.length;i<size;i++){
			send[i] = expansion; //fill the leading values due to low 0 sig (Little Endian)
		}
		return send;
	}
	
	//Gets the greatest bit flipped '1' in the array
	protected static int getGreatestBit(byte[] a){ //O(n)
		if (a == null) throw new NullPointerException("The array must actually exist");
		if (a.length == 0) return 0; //nothing to search through
		//returns 0 if no bits are flipped
		int mask;
		for (int i=a.length-1;i>=0;i--){
			if (a[i] ==0) continue; //no bits, move on
			mask = 0x80;
			for (int n=7;n>=0;n--){
				if ((((a[i]&mask) >>> n) &0b1) == 1) return i*8+n;
				mask >>=1;
			}
		}
		return 0;
	}
	
	//Returns True / False depending on the bit at the position specified
	protected static boolean getBit(byte[] a, int pos){ //O(1)
		if (pos>>>3 >= a.length) return false; //throw new IllegalArgumentException("Position must be within the bounds of the array, got " + pos + ", expected a maximum value of " + (a.length*8-1));
		return ((a[pos>>>3]>>>(pos&0b111)) &0b1) == 1;
	}
	protected static void setBit(byte[] a, int pos, boolean value){ //TODO: Test
		if (pos>>>3 >= a.length) return;
		if (value) a[pos>>>3] |= (1 << (pos&0b111));
		else a[pos>>>3] &= ~(1 << (pos&0b111));
	}
	
	//Generate the byte sized carries needed to perform an incredibly quick addition operation
	public static byte genCarries(byte a, byte b, boolean[] c){
		byte g = (byte)(a & b); //carries generated guarenteed
		byte p = (byte)(a | b); //carries will be propegated
		byte send = 0;
		//send |= (g&0b1); //initalise send
		//g >>>= 1;
		//System.out.print("Debug: " + g + ", " + p + ", ");
		if (c == null ? false : c.length > 0) send = (byte)(c[0]?1:0); //if the carry given in was true, it'll help produce more carries in the rest of the byte
		for (int i=0;i<7;i++){
			send <<= 1;
			send |= (g&0b1); //add the generation wave
			send |= ((p&0b1) & ((send>>1) & 0b1));
			
			g=(byte)((g&0xff)>>>1); //0xff required to go from byte to int (forced) without carrying over all the 1's that might exist there representing a negative number
			p=(byte)((p&0xff)>>>1);
			//System.out.println("\n" + Debug.toBinaryString(g) + ", " + Debug.toBinaryString(p) + ", " + Debug.toBinaryString(send));
			
		}
		//System.out.print(send);
		
		//reverse the bits on the send byte
		send = (byte)((send & 0xF0) >>> 4 | (send & 0x0F) << 4); // Swap nibbles
		send = (byte)((send & 0xCC) >>> 2 | (send & 0x33) << 2); // Swap pairs
		send = (byte)((send & 0xAA) >>> 1 | (send & 0x55) << 1); // Swap bits
		//System.out.println(", " + g + ", " + p + ", " + send);
		if (c==null?false:(c.length > 0)){ //update the super carry
			//System.out.print("DEBUG2: C[0]="+c[0]+"->");
			c[0] = ((g&0b1) | ((p&0b1) & ((send>>7) & 0b1))) == 1;
			//System.out.println(c[0] + ", " + g + ", " + p + ", " + send);
		}
		
		return send;
	}
	public static byte genCarries(byte a, byte b){
		return genCarries(a, b, new boolean[1]);
	}
	
	//reverses the order of the bits throughout the entire array
	public static byte[] reverse(byte[] a){
		byte[] send;
		send = reverseBytes(a);
		
		//Reverse the bit order in each byte
		for (int i=0;i<a.length;i++){
			send[i] = (byte)((send[i] & 0xF0) >>> 4 | (send[i] & 0x0F) << 4); // Swap nibbles
			send[i] = (byte)((send[i] & 0xCC) >>> 2 | (send[i] & 0x33) << 2); // Swap pairs
			send[i] = (byte)((send[i] & 0xAA) >>> 1 | (send[i] & 0x55) << 1); // Swap bits
		}
		return send;
	}
	
	//reverses the order of the bytes while preserving the bit order - used with the byte translater with "high 0 significance" (Big Endian) while we use "low 0 significance" (Little Endian)
	public static byte[] reverseBytes(byte[] a){
		byte[] send = new byte[a.length];
		for (int i=0;i<a.length;i++){
			send[a.length-1-i] = a[i];
		}
		return send;
	}
	protected static byte[] reverseBytesR(byte[] a){ //reverses the order of the bytes in place
		byte temp = 0;
		for (int i=0;i<a.length/2;i++){
			temp = a[i];
			a[i] = a[a.length-1-i];
			a[a.length-1-i] = temp;
		}
		return a;
	}
	
	//trims down the size of the byte arrays into a more manigable size
	public static byte[] trim(byte[] a){
		if (a == null) throw new NullPointerException("Array to be trimmed must actually exist");
		if (a.length == 0) throw new IllegalArgumentException("Array to be trimmed must have at least 1 byte to trim");
		int count = a.length;
		boolean allNegative = a[a.length-1] == -1; //if the significant byte is all 1s, meaning the value represented is a negative one
		for (int i=a.length-1;i>=0;i--){
			if (a[i] == 0 && !allNegative && (i>0 ? ((a[i-1]>>>7)&0b1)==0 : false)) count --; //leading 0s can be removed from memory when the next byte doesn't have a negative number - 0s are implied - positive values are preserved
			else if (a[i] == -1 && allNegative && (i>0 ? ((a[i-1]>>>7)&0b1)==1 : false)) count--; //leading 1s can be removed if the next byte has a leading 1 - negative values are preserved
			else break;
		}
		
		byte[] send = new byte[count];
		System.arraycopy(a, 0, send, 0, count);
		return send;
	}
	
	//Turn a byte array into a base 9* representation as a string
	public static String toDecString(byte[] a){
		return toString(a, (byte)9);
	}
	public static String toString(byte[] a, byte base){ //pass in the base according to the notes in Debug.java I.E numbers 0-9 is base 9, 0-1:Base 1, 0-F:Base E
		if (a == null) throw new NullPointerException("Array must actually exist");
		if (a.length == 0) throw new IllegalArgumentException("Array to be trimmed must have at least 1 byte to trim");
		LinkedList<Character> send = new LinkedList<>();
		byte[] divisor = new byte[] {(byte)((base&(~(1<<7))) + 1)}; //no negative bases presereved
		
		byte[] dividend = new byte[a.length];
		if (isNegative(a)) dividend = negate(a);
		else System.arraycopy(a, 0, dividend, 0, a.length);
		
		byte[] remainder = new byte[1];
		
		//System.out.println(Debug.toString(dividend));
		do{
			dividend = divide(dividend, divisor, remainder);
			send.add((char)(remainder[0]+48));
			//remainder[0] = 0;
		} while (!equals(dividend, new byte[] {0}));
		
		if (isNegative(a)) send.add('-');
		
		char[] sendString = new char[send.size()];
		int index = send.size()-1;
		for (Character item : send){
			sendString[index--] = item.charValue();
		}
		return new String(sendString);
	}
	
}