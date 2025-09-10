
import java.util.Scanner;
import java.util.Random;
import helpful.connection.ByteTranslater;

class Debug{
	public static void main(String args[]){
		//System.out.println(toString(BAMath.trim(new byte[] {(byte)0x00,(byte)0x00,(byte)0x28,(byte)0x00})));
		new Debug();
	}
	
	public Debug(){
		Scanner scan = new Scanner(System.in);
		String res = "";
		int choice = 0;
		Random rand = new Random();
		
		System.out.println("Flags:\n-e: Expanded unit test explanations\n-n: Prints the names of each test with its results\n-c: Prints a True / False of if all tests ran passed or not\n-r: Runs the specified tests with the replace array function if the function exists");
		validFlags = new char[] {'e', 'n', 'c', 'r'};
		
		while (!res.equals("0") && !res.equals("quit")){
			System.out.println("\n\nWhat would you like to test?"
							  +"\n00: Quit\n01: Addition Carry Generation"
							  +"\n02: Byte Array Trimming"
							  +"\n03: Byte Array Reversal"
							  +"\n04: Byte Array Addition"
							  +"\n05: Byte Array Flipping"
							  +"\n06: Byte Array Expansion"
							  +"\n07: Byte Array Subtraction"
							  +"\n08: Byte Array Shift"
							  +"\n09: Byte Array Multiplication"
							  +"\n10: Byte Array Comparisons"
							  +"\n11: Byte Array Division"
							  +"\n12: Byte Array Modulo"
							  +"\n13: Base 9* Conversion"
							  +"\n14: Byte Array Modulo Multiplication"
							  +"\n100: All tests");
			res = scan.nextLine().toLowerCase();
			boolean onlyDigits = true, fullCarry = true;
			flags = 0; //reset all flags
			choice = 0; //reset the choice
			for (char item : res.toCharArray()){
				if (Character.isDigit(item) && onlyDigits){
					choice *= 10;
					choice += (int)item - 48;
				}
				if (Character.isLetter(item)){
					onlyDigits = false;
					addFlag(item);
				}
			}
			
			//Test variables
			byte[] a, b, out, expected;
			boolean test;
			
			if (choice == 1 || choice == 100){ //carry generation testing
				byte ab=(byte)0b01110011, 
					 bb=(byte)0b11010010;
				boolean[] carry = new boolean[1];
				test = ((byte)(ab+bb) == (ab^bb^BAMath.genCarries(ab,bb,carry)) && carry[0]);
				System.out.print("\nTest 1.1: " + (test ? "PASSED":"FAIL"));
				if (getFlag('n')) System.out.print(" - Set Addition");
				if (getFlag('e')) System.out.print(" - A: " + toString(ab) + ", B: " + toString(bb) + ", Out: " + toString(BAMath.genCarries(ab,bb)) + ", Actual sum: " + (byte)(ab+bb) + " Believed sum: " + (ab^bb^BAMath.genCarries(ab,bb)) +", Super Carry:" + carry[0]);
				if (getFlag('c')) fullCarry &= test;
				
				ab = (byte)rand.nextInt();
				bb = (byte)rand.nextInt();
				carry[0] = false;
				test = (byte)(ab+bb) == (ab^bb^BAMath.genCarries(ab,bb,carry));
				System.out.print("\nTest 1.2: " + (test ? "PASSED":"FAIL"));
				if (getFlag('n')) System.out.print(" - Randomized Addition");
				if (getFlag('e')) System.out.print(" - A: " + toString(ab) + ", B: " + toString(bb) + ", Out: " + toString(BAMath.genCarries(ab,bb)) + ", Actual sum: " + (byte)(ab+bb) + ", Believed sum: " + (ab^bb^BAMath.genCarries(ab,bb)) +", Super Carry:" + carry[0]);
				if (getFlag('c')) fullCarry &= test;
				
				ab = (byte)0x1f;
				bb = (byte)0xaa;
				carry[0] = false;
				test = ((byte)(ab+bb) == (ab^bb^BAMath.genCarries(ab,bb,carry)) && !carry[0]);
				System.out.print("\nTest 1.3: " + (test ? "PASSED":"FAIL"));
				if (getFlag('n')) System.out.print(" - Problem Set Addition");
				if (getFlag('e')) System.out.print(" - A: " + toString(ab) + ", B: " + toString(bb) + ", Out: " + toString(BAMath.genCarries(ab,bb)) + ", Actual sum: " + (byte)(ab+bb) + ", Believed sum: " + (ab^bb^BAMath.genCarries(ab,bb)) +", Super Carry:" + carry[0]);
				if (getFlag('c')) fullCarry &= test;
				
				ab = (byte)0x55;
				bb = (byte)0xf8;
				carry[0] = false;
				test = ((byte)(ab+bb) == (ab^bb^BAMath.genCarries(ab,bb,carry)) && carry[0]);
				System.out.print("\nTest 1.4: " + (test ? "PASSED":"FAIL"));
				if (getFlag('n')) System.out.print(" - Problem Set Addition Pt. 2");
				if (getFlag('e')) System.out.print(" - A: " + toString(ab) + ", B: " + toString(bb) + ", Out: " + toString(BAMath.genCarries(ab,bb)) + ", Actual sum: " + (byte)(ab+bb) + ", Believed sum: " + (ab^bb^BAMath.genCarries(ab,bb)) +", Super Carry:" + carry[0]);
				if (getFlag('c')) fullCarry &= test;
				
				System.out.println();
			}
			
			if (choice == 2 || choice == 100){ //byte array trimming testing
				a = new byte[] {0b01110011};
				test = equals(a, BAMath.trim(a));
				System.out.print("\nTest 2.1: " + (test ? "PASSED":"FAIL"));
				if (getFlag('n')) System.out.print(" - Set No Trim");
				if (getFlag('e')) System.out.print(" - A: " + toString(a) + ", Out: " + toString(BAMath.trim(a)) + "  =>  A: " + toBinaryString(a) + ", Out: " + toBinaryString(BAMath.trim(a)));
				if (getFlag('c')) fullCarry &= test;
				
				a = new byte[] {0b01110011, 0b0};
				test = equals(new byte[] {0b01110011}, BAMath.trim(a));
				System.out.print("\nTest 2.2: " + (test ? "PASSED":"FAIL"));
				if (getFlag('n')) System.out.print(" - Set Trim Leading Zeros");
				if (getFlag('e')) System.out.print(" - A: " + toString(a) + ", Out: " + toString(BAMath.trim(a)) + "  =>  A: " + toBinaryString(a) + ", Out: " + toBinaryString(BAMath.trim(a)));
				if (getFlag('c')) fullCarry &= test;
				
				a = new byte[] {0b01110011, (byte)0b11111111};
				test = equals(a, BAMath.trim(a));
				System.out.print("\nTest 2.3: " + (test ? "PASSED":"FAIL"));
				if (getFlag('n')) System.out.print(" - Set No Trim Leading Ones");
				if (getFlag('e')) System.out.print(" - A: " + toString(a) + ", Out: " + toString(BAMath.trim(a)) + "  =>  A: " + toBinaryString(a) + ", Out: " + toBinaryString(BAMath.trim(a)));
				if (getFlag('c')) fullCarry &= test;
				
				a = new byte[] {(byte)0b11110011, (byte)0b11111111};
				test = equals(new byte[] {(byte)0b11110011}, BAMath.trim(a));
				System.out.print("\nTest 2.4: " + (test ? "PASSED":"FAIL"));
				if (getFlag('n')) System.out.print(" - Set Trim Leading Ones");
				if (getFlag('e')) System.out.print(" - A: " + toString(a) + ", Out: " + toString(BAMath.trim(a)) + "  =>  A: " + toBinaryString(a) + ", Out: " + toBinaryString(BAMath.trim(a)));
				if (getFlag('c')) fullCarry &= test;
				
				a = new byte[] {(byte)0xab, 0x40, 0x4d, 0x1, 0x1}; //1 014D 40AB
				test = equals(a, BAMath.trim(a));
				System.out.print("\nTest 2.5: " + (test ? "PASSED":"FAIL"));
				if (getFlag('n')) System.out.print(" - Set Trim No Change Problem Set");
				if (getFlag('e')) System.out.print(" - A: " + toString(a) + ", Out: " + toString(BAMath.trim(a)) + "  =>  A: " + toBinaryString(a) + ", Out: " + toBinaryString(BAMath.trim(a)));
				if (getFlag('c')) fullCarry &= test;
				
				a = new byte[] {(byte)0b11110011, 0b0};
				test = equals(new byte[] {(byte)0b11110011, 0b0}, BAMath.trim(a));
				System.out.print("\nTest 2.6: " + (test ? "PASSED":"FAIL"));
				if (getFlag('n')) System.out.print(" - Set No Trim - Preserve Positiveness");
				if (getFlag('e')) System.out.print(" - A: " + toString(a) + ", Out: " + toString(BAMath.trim(a)) + "  =>  A: " + toBinaryString(a) + ", Out: " + toBinaryString(BAMath.trim(a)));
				if (getFlag('c')) fullCarry &= test;
			
				System.out.println();
			}
			
			if (choice == 3 || choice == 100){ //byte array reversal
				a = new byte[] {(byte)0b00000001};
				test = equals(BAMath.reverse(a), new byte[] {(byte)0b10000000});
				System.out.print("\nTest 3.1: " + (test ? "PASSED":"FAIL"));
				if (getFlag('n')) System.out.print(" - Single Byte Reversal");
				if (getFlag('e')) System.out.print(" - A: " + toString(a) + ", Out: " + toString(BAMath.reverse(a)));
				if (getFlag('c')) fullCarry &= test;
				
				a = new byte[] {(byte)0b10000000, (byte)0b10010010};
				test = equals(BAMath.reverse(a), new byte[] {(byte)0b01001001, (byte)0b00000001});
				System.out.print("\nTest 3.2: " + (test ? "PASSED":"FAIL"));
				if (getFlag('n')) System.out.print(" - Double Byte Reversal");
				if (getFlag('e')) System.out.print(" - A: " + toString(a) + ", Out: " + toString(BAMath.reverse(a)));
				if (getFlag('c')) fullCarry &= test;
				
				a = new byte[] {(byte)0b10000000, (byte)0b00010000, (byte)0b10000000};
				test = equals(BAMath.reverse(a), new byte[] {(byte)0b00000001, (byte)0b00001000, (byte)0b00000001});
				System.out.print("\nTest 3.3: " + (test ? "PASSED":"FAIL"));
				if (getFlag('n')) System.out.print(" - Odd Byte Reversal");
				if (getFlag('e')) System.out.print(" - A: " + toString(a) + ", Out: " + toString(BAMath.reverse(a)));
				if (getFlag('c')) fullCarry &= test;
				
				System.out.println();
			}
			
			if (choice == 4 || choice == 100){ //byte array addition testing
				a = new byte[] {0b01010101, (byte)0b00000001};
				b = new byte[] {(byte)0b10101011, (byte)0b00000000};
				out = BAMath.add(a, b);
				if (getFlag('r')) out = BAMath.addR(a,b);
				test = equals(out, new byte[] {(byte)0b0, (byte)0b10});
				System.out.print("\nTest 4.1: " + (test ? "PASSED":"FAIL"));
				if (getFlag('n')) System.out.print(" - Set Array Addition");
				if (getFlag('e')) System.out.print(" - A: " + toString(a) + ", B: " + toString(b) + ", Out: " + toString(out));
				if (getFlag('c')) fullCarry &= test;
				
				a = new byte[] {0b01010101, (byte)0b00000001};
				b = new byte[] {(byte)0b10101011};
				out = BAMath.add(a, b);
				if (getFlag('r')) out = BAMath.addR(a,b);
				test = equals(out, new byte[] {(byte)0b0, (byte)0b10});
				System.out.print("\nTest 4.2: " + (test ? "PASSED":"FAIL"));
				if (getFlag('n')) System.out.print(" - Mixed Length Byte Addition");
				if (getFlag('e')) System.out.print(" - A: " + toString(a) + ", B: " + toString(b) + ", Out: " + toString(out));
				if (getFlag('c')) fullCarry &= test;
				
				a = new byte[] {(byte)0x7a, (byte)0x01};
				b = new byte[] {(byte)0x60, (byte)0x03};
				out = BAMath.add(a, b);
				if (getFlag('r')) out = BAMath.addR(a,b);
				test = equals(out, new byte[] {(byte)0xda, (byte)0x04});
				System.out.print("\nTest 4.3: " + (test ? "PASSED":"FAIL"));
				if (getFlag('n')) System.out.print(" - Addition problem set");
				if (getFlag('e')) System.out.print(" - A: " + toString(a) + ", B: " + toString(b) + ", Out: " + toString(out));
				if (getFlag('c')) fullCarry &= test;
				
				a = ByteTranslater.toBytes(rand.nextInt());//0x30AA4433);
				b = ByteTranslater.toBytes(rand.nextInt());//0x2F1FB8FB);
				out = BAMath.reverseBytes(ByteTranslater.subArray(BAMath.add(BAMath.reverseBytes(a), BAMath.reverseBytes(b)),0,4)); //keep the size less than four for the super carry cases
				test = equals(out, ByteTranslater.toBytes(ByteTranslater.toInt(a) + ByteTranslater.toInt(b)));
				System.out.print("\nTest 4.4: " + (test ? "PASSED":"FAIL"));
				if (getFlag('n')) System.out.print(" - Random Array Addition");
				if (getFlag('e')) System.out.print(" - A: " + toString(a) + ", B: " + toString(b) + ", Out: " + toString(out) + ", Expected: " + toString(ByteTranslater.toBytes(ByteTranslater.toInt(a) + ByteTranslater.toInt(b))));
				if (getFlag('c')) fullCarry &= test;
				
				a = new byte[] {(byte)0xaa, (byte)0xaa};
				b = new byte[] {(byte)0x5f, (byte)0x55};
				out = BAMath.add(a, b);
				if (getFlag('r')) out = BAMath.addR(a,b);
				expected = new byte[] {(byte)0x09};
				if (getFlag('r')) expected = new byte[] {(byte)0x09, (byte)0x00};
				test = equals(out, expected);
				System.out.print("\nTest 4.5: " + (test ? "PASSED":"FAIL"));
				if (getFlag('n')) System.out.print(" - Negative Addition To Positive");
				if (getFlag('e')) System.out.print(" - A: " + toString(a) + ", B: " + toString(b) + ", Out: " + toString(out) + ", Expected: " + toString(expected));
				if (getFlag('c')) fullCarry &= test;
				
				System.out.println();
			}
			
			if (choice == 5 || choice == 100){ //Byte array flipping (0->1,1->0)
				a = new byte[] {0b01010101, (byte)0b00000001};
				out = BAMath.flip(a);
				if (getFlag('r')) out = BAMath.flipR(a);
				test = equals(out, new byte[] {(byte)0b010101010, (byte)0b11111110});
				System.out.print("\nTest 5.1: " + (test ? "PASSED":"FAIL"));
				if (getFlag('n')) System.out.print(" - Set Array Flipping");
				if (getFlag('e')) System.out.print(" - A: " + toString(a) + ", Out: " + toString(out));
				if (getFlag('c')) fullCarry &= test;
				
				a = new byte[] {(byte)0b0};
				out = BAMath.flip(a);
				if (getFlag('r')) out = BAMath.flipR(a);
				test = equals(out, new byte[] {(byte)0xff});
				System.out.print("\nTest 5.2: " + (test ? "PASSED":"FAIL"));
				if (getFlag('n')) System.out.print(" - Set Array Flipping Pt. 2");
				if (getFlag('e')) System.out.print(" - A: " + toString(a) + ", Out: " + toString(out));
				if (getFlag('c')) fullCarry &= test;
				
				a = new byte[] {(byte)rand.nextInt()};
				out = BAMath.flip(a);
				test = equals(out, new byte[] {(byte)~a[0]});
				System.out.print("\nTest 5.3: " + (test ? "PASSED":"FAIL"));
				if (getFlag('n')) System.out.print(" - Random Array Flipping");
				if (getFlag('e')) System.out.print(" - A: " + toString(a) + ", Out: " + toString(out));
				if (getFlag('c')) fullCarry &= test;
				
				
				System.out.println();
			}
			
			if (choice == 6 || choice == 100){ //Byte array expansion
				a = new byte[] {(byte)0};
				out = BAMath.expand(a, 2);
				test = equals(out, new byte[] {(byte)0, (byte)0});
				System.out.print("\nTest 6.1: " + (test ? "PASSED":"FAIL"));
				if (getFlag('n')) System.out.print(" - Simple Expansion");
				if (getFlag('e')) System.out.print(" - A: " + toString(a) + ", Out: " + toString(out));
				if (getFlag('c')) fullCarry &= test;
				
				a = new byte[] {(byte)0x15};
				out = BAMath.expand(a, 2);
				test = equals(out, new byte[] {(byte)0x15, 0});
				System.out.print("\nTest 6.2: " + (test ? "PASSED":"FAIL"));
				if (getFlag('n')) System.out.print(" - Data Preservation Expansion");
				if (getFlag('e')) System.out.print(" - A: " + toString(a) + ", Out: " + toString(out));
				if (getFlag('c')) fullCarry &= test;
				
				a = new byte[] {(byte)0x94};
				out = BAMath.expand(a, 2);
				test = equals(out, new byte[] {(byte)0x94, (byte)0xff});
				System.out.print("\nTest 6.3: " + (test ? "PASSED":"FAIL"));
				if (getFlag('n')) System.out.print(" - Data Preservation Negative Expansion");
				if (getFlag('e')) System.out.print(" - A: " + toString(a) + ", Out: " + toString(out));
				if (getFlag('c')) fullCarry &= test;
				
				a = new byte[] {(byte)0x94, (byte)0xa5, (byte)0x76};
				out = BAMath.expand(a, 6);
				test = equals(out, new byte[] {(byte)0x94, (byte)0xa5, (byte)0x76, 0, 0, 0});
				System.out.print("\nTest 6.4: " + (test ? "PASSED":"FAIL"));
				if (getFlag('n')) System.out.print(" - Data Preservation Large Expansion");
				if (getFlag('e')) System.out.print(" - A: " + toString(a) + ", Out: " + toString(out));
				if (getFlag('c')) fullCarry &= test;
				
				a = new byte[] {(byte)0x94, (byte)0xa5, (byte)0xb6};
				out = BAMath.expand(a, 6);
				test = equals(out, new byte[] {(byte)0x94, (byte)0xa5, (byte)0xb6, (byte)0xff, (byte)0xff, (byte)0xff});
				System.out.print("\nTest 6.5: " + (test ? "PASSED":"FAIL"));
				if (getFlag('n')) System.out.print(" - Data Preservation Large Negative Expansion");
				if (getFlag('e')) System.out.print(" - A: " + toString(a) + ", Out: " + toString(out));
				if (getFlag('c')) fullCarry &= test;
				
				
				System.out.println();
			}
			
			if (choice == 7 || choice == 100){ //Byte array subtraction
				a = new byte[] {(byte)10};
				b = new byte[] {(byte)5};
				out = BAMath.subtract(a, b);
				if (getFlag('r')) out = BAMath.subtractR(a,b);
				test = equals(out, new byte[] {(byte)5});
				System.out.print("\nTest 7.1: " + (test ? "PASSED":"FAIL"));
				if (getFlag('n')) System.out.print(" - Single Small Byte Subtraction");
				if (getFlag('e')) System.out.print(" - A: " + toString(a) + ", B: " + toString(b) + ", Out: " + toString(out));
				if (getFlag('c')) fullCarry &= test;
				
				a = new byte[] {(byte)0x0, (byte)0xff};
				b = new byte[] {(byte)0xff, (byte)0x0};
				out = BAMath.subtract(a, b);
				if (getFlag('r')) out = BAMath.subtractR(a,b);
				test = equals(out, new byte[] {(byte)0x01, (byte)0xfe});
				System.out.print("\nTest 7.2: " + (test ? "PASSED":"FAIL"));
				if (getFlag('n')) System.out.print(" - Mixed Length Byte Subtraction");
				if (getFlag('e')) System.out.print(" - A: " + toString(a) + ", B: " + toString(b) + ", Out: " + toString(out));
				if (getFlag('c')) fullCarry &= test;
				
				a = ByteTranslater.toBytes(rand.nextInt());
				b = ByteTranslater.toBytes(rand.nextInt());
				expected = ByteTranslater.toBytes(ByteTranslater.toInt(a) - ByteTranslater.toInt(b));
				out = BAMath.reverseBytes(ByteTranslater.subArray(BAMath.subtract(BAMath.reverseBytes(a), BAMath.reverseBytes(b)),0,4)); //keep the size less than four for the super carry cases
				test = equals(out, expected);
				System.out.print("\nTest 7.3: " + (test ? "PASSED":"FAIL"));
				if (getFlag('n')) System.out.print(" - Random Array Subtraction");
				if (getFlag('e')) System.out.print(" - A: " + toString(a) + ", B: " + toString(b) + ", Out: " + toString(out) + ", Expected: " + toString(expected));
				if (getFlag('c')) fullCarry &= test;
				
				a = new byte[] {(byte)0xff, 0x0};
				b = new byte[] {(byte)0x01, 0x0};
				out = BAMath.subtract(a, b);
				if (getFlag('r')) out = BAMath.subtractR(a,b);
				test = equals(out, new byte[] {(byte)0xfe, (byte)0x00});
				System.out.print("\nTest 7.4: " + (test ? "PASSED":"FAIL"));
				if (getFlag('n')) System.out.print(" - Array Size Change Subtraction");
				if (getFlag('e')) System.out.print(" - A: " + toString(a) + ", B: " + toString(b) + ", Out: " + toString(out));
				if (getFlag('c')) fullCarry &= test;
				
				a = new byte[] {(byte)0x5f, (byte)0x55};
				b = new byte[] {(byte)0xaa, (byte)0xaa};
				out = BAMath.subtract(a, b);
				if (getFlag('r')) out = BAMath.subtractR(a,b);
				test = equals(out, new byte[] {(byte)0xb5, (byte)0xaa});
				System.out.print("\nTest 7.4: " + (test ? "PASSED":"FAIL"));
				if (getFlag('n')) System.out.print(" - Negative subtraction");
				if (getFlag('e')) System.out.print(" - A: " + toString(a) + ", B: " + toString(b) + ", Out: " + toString(out));
				if (getFlag('c')) fullCarry &= test;
				
				
				System.out.println();
			}
			
			if (choice == 8 || choice == 100){ //Byte array shiftings
				a = new byte[] {(byte)0b10};
				out = BAMath.rShift(a, 1);
				expected = new byte[] {(byte)0b1};
				if (getFlag('r')) out = BAMath.rShiftR(a, 1); //replace output with other function
				test = equals(out, expected);
				System.out.print("\nTest 8.1: " + (test ? "PASSED":"FAIL"));
				if (getFlag('n')) System.out.print(" - Single Bit Right Shift");
				if (getFlag('e')) System.out.print(" - A: " + toString(a) + ", Out: " + toString(out) + ", Expected: " + toString(expected));
				if (getFlag('c')) fullCarry &= test;
				
				a = new byte[] {(byte)0b1010};
				out = BAMath.rShift(a, 1);
				expected = new byte[] {(byte)0b101};
				if (getFlag('r')) out = BAMath.rShiftR(a, 1);
				test = equals(out, expected);
				System.out.print("\nTest 8.2: " + (test ? "PASSED":"FAIL"));
				if (getFlag('n')) System.out.print(" - Multi Bit Single Byte Right Shift");
				if (getFlag('e')) System.out.print(" - A: " + toString(a) + ", Out: " + toString(out) + ", Expected: " + toString(expected));
				if (getFlag('c')) fullCarry &= test;
				
				a = new byte[] {(byte)0x00, (byte)0x01};
				out = BAMath.rShift(a, 1);
				expected = new byte[] {(byte)0x80, (byte)0x0};
				if (getFlag('r')) out = BAMath.rShiftR(a, 1);
				test = equals(out, expected);
				System.out.print("\nTest 8.3: " + (test ? "PASSED":"FAIL"));
				if (getFlag('n')) System.out.print(" - Single Bit Cross Byte Right Shift");
				if (getFlag('e')) System.out.print(" - A: " + toString(a) + ", Out: " + toString(out) + ", Expected: " + toString(expected));
				if (getFlag('c')) fullCarry &= test;
				
				a = new byte[] {(byte)0x00, (byte)0b1001};
				out = BAMath.rShift(a, 1);
				expected = new byte[] {(byte)0b10000000, (byte)0b100};
				if (getFlag('r')) out = BAMath.rShiftR(a, 1);
				test = equals(out, expected);
				System.out.print("\nTest 8.4: " + (test ? "PASSED":"FAIL"));
				if (getFlag('n')) System.out.print(" - Multi Bit Cross Byte Right Shift");
				if (getFlag('e')) System.out.print(" - A: " + toString(a) + ", Out: " + toString(out) + ", Expected: " + toString(expected));
				if (getFlag('c')) fullCarry &= test;
				
				a = new byte[] {(byte)0x00, (byte)0b1001};
				out = BAMath.rShift(a, 4);
				expected = new byte[] {(byte)0b10010000, (byte)0b0};
				if (getFlag('r')) out = BAMath.rShiftR(a, 4);
				test = equals(out, expected);
				System.out.print("\nTest 8.5: " + (test ? "PASSED":"FAIL"));
				if (getFlag('n')) System.out.print(" - Multi Bit Cross Byte Right Shift 4");
				if (getFlag('e')) System.out.print(" - A: " + toString(a) + ", Out: " + toString(out) + ", Expected: " + toString(expected));
				if (getFlag('c')) fullCarry &= test;
				
				a = new byte[] {(byte)0x00, (byte) 0x00, (byte)0b00001001};
				out = BAMath.rShift(a, 12);
				expected = new byte[] {(byte)0b10010000, (byte)0b0};
				if (getFlag('r')) {out = BAMath.rShiftR(a, 12);
					expected = new byte[] {(byte)0x90, (byte)0x0, (byte)0x0};}
				test = equals(out, expected);
				System.out.print("\nTest 8.6: " + (test ? "PASSED":"FAIL"));
				if (getFlag('n')) System.out.print(" - Multi Bit Cross Byte Right Shift 12");
				if (getFlag('e')) System.out.print(" - A: " + toString(a) + ", Out: " + toString(out) + ", Expected: " + toString(expected));
				if (getFlag('c')) fullCarry &= test;
				
				
				//Left Shift testing
				a = new byte[] {(byte)0b10};
				out = BAMath.lShift(a, 1);
				expected = new byte[] {(byte)0b100};
				if (getFlag('r')) out = BAMath.lShiftR(a, 1); //replace output with other function
				test = equals(out, expected);
				System.out.print("\nTest 8.7: " + (test ? "PASSED":"FAIL"));
				if (getFlag('n')) System.out.print(" - Single Bit Left Shift");
				if (getFlag('e')) System.out.print(" - A: " + toString(a) + ", Out: " + toString(out) + ", Expected: " + toString(expected));
				if (getFlag('c')) fullCarry &= test;
				
				a = new byte[] {(byte)0b1010};
				out = BAMath.lShift(a, 1);
				expected = new byte[] {(byte)0b10100};
				if (getFlag('r')) out = BAMath.lShiftR(a, 1);
				test = equals(out, expected);
				System.out.print("\nTest 8.8: " + (test ? "PASSED":"FAIL"));
				if (getFlag('n')) System.out.print(" - Multi Bit Single Byte Left Shift");
				if (getFlag('e')) System.out.print(" - A: " + toString(a) + ", Out: " + toString(out) + ", Expected: " + toString(expected));
				if (getFlag('c')) fullCarry &= test;
				
				a = new byte[] {(byte)0x80};
				out = BAMath.lShift(a, 1);
				expected = new byte[] {(byte)0x00, (byte)0x01};
				//if (getFlag('r')) out = BAMath.lShiftR(a, 1); //no list expansion - cannot pass
				test = equals(out, expected);
				System.out.print("\nTest 8.9: " + (test ? "PASSED":"FAIL"));
				if (getFlag('n')) System.out.print(" - Single Bit Cross Byte Left Shift");
				if (getFlag('e')) System.out.print(" - A: " + toString(a) + ", Out: " + toString(out) + ", Expected: " + toString(expected));
				if (getFlag('c')) fullCarry &= test;
				
				a = new byte[] {(byte)0b10010000};
				out = BAMath.lShift(a, 1);
				//if (getFlag('r')) out = BAMath.lShiftR(a, 1); //no list expansion - cannot pass
				expected = new byte[] {(byte)0b00100000, (byte)0b1};
				test = equals(out, expected);
				System.out.print("\nTest 8.A: " + (test ? "PASSED":"FAIL"));
				if (getFlag('n')) System.out.print(" - Multi Bit Cross Byte Left Shift");
				if (getFlag('e')) System.out.print(" - A: " + toString(a) + ", Out: " + toString(out) + ", Expected: " + toString(expected));
				if (getFlag('c')) fullCarry &= test;
				
				a = new byte[] {(byte)0b10010000};
				out = BAMath.lShift(a, 4);
				expected = new byte[] {(byte)0x00, (byte)0b1001}; 
				//if (getFlag('r')) out = BAMath.lShiftR(a, 4); //no list expansion - cannot pass
				test = equals(out, expected);
				System.out.print("\nTest 8.B: " + (test ? "PASSED":"FAIL"));
				if (getFlag('n')) System.out.print(" - Multi Bit Cross Byte Left Shift 4");
				if (getFlag('e')) System.out.print(" - A: " + toString(a) + ", Out: " + toString(out) + ", Expected: " + toString(expected));
				if (getFlag('c')) fullCarry &= test;
				
				a = new byte[] {(byte)0b00001001, (byte)0x00};
				out = BAMath.lShift(a, 12);
				expected = new byte[] {(byte)0x00, (byte)0b10010000};
				if (getFlag('r')) out = BAMath.lShiftR(a, 12);
				test = equals(out, expected);
				System.out.print("\nTest 8.C: " + (test ? "PASSED":"FAIL"));
				if (getFlag('n')) System.out.print(" - Multi Bit Cross Byte Left Shift 12");
				if (getFlag('e')) System.out.print(" - A: " + toString(a) + ", Out: " + toString(out) + ", Expected: " + toString(expected));
				if (getFlag('c')) fullCarry &= test;
				
				a = BAMath.reverseBytes(new byte[] {(byte)0x55, (byte)0xaa, (byte)0x55});
				out = BAMath.lShift(a, 19);
				expected = BAMath.reverseBytes(new byte[] {(byte)0x2, (byte)0xAD, (byte)0x52, (byte)0xA8, (byte)0x00, (byte)0x00});
				test = equals(out, expected);
				System.out.print("\nTest 8.D: " + (test ? "PASSED":"FAIL"));
				if (getFlag('n')) System.out.print(" - Problem Set");
				if (getFlag('e')) System.out.print(" - A: " + toString(a) + ", Out: " + toString(out) + ", Expected: " + toString(expected));
				if (getFlag('c')) fullCarry &= test;
				
				
				System.out.println();
			}
			
			if (choice == 9 || choice == 100){ //Byte array multiplication
				a = new byte[] {(byte)0b10};
				b = new byte[] {(byte)0b10};
				out = BAMath.multiply(a, b);
				expected = new byte[] {(byte)0b100};
				test = equals(out, expected);
				System.out.print("\nTest 9.1: " + (test ? "PASSED":"FAIL"));
				if (getFlag('n')) System.out.print(" - Multiplication Shift");
				if (getFlag('e')) System.out.print(" - A: " + toString(a) + ", B: " + toString(b) + ", Out: " + toString(out) + ", Expected: " + toString(expected));
				if (getFlag('c')) fullCarry &= test;
				
				a = new byte[] {(byte)0b11};
				b = new byte[] {(byte)0b10};
				out = BAMath.multiply(a, b);
				expected = new byte[] {(byte)0b110};
				test = equals(out, expected);
				System.out.print("\nTest 9.2: " + (test ? "PASSED":"FAIL"));
				if (getFlag('n')) System.out.print(" - Multiplication Shift Pt.2");
				if (getFlag('e')) System.out.print(" - A: " + toString(a) + ", B: " + toString(b) + ", Out: " + toString(out) + ", Expected: " + toString(expected));
				if (getFlag('c')) fullCarry &= test;
				
				a = new byte[] {(byte)0x36};
				b = new byte[] {(byte)0x17};
				out = BAMath.multiply(a, b);
				expected = new byte[] {(byte)0xda, (byte)0x4};
				test = equals(out, expected);
				System.out.print("\nTest 9.3: " + (test ? "PASSED":"FAIL"));
				if (getFlag('n')) System.out.print(" - Set Larger Multiplication");
				if (getFlag('e')) System.out.print(" - A: " + toString(a) + ", B: " + toString(b) + ", Out: " + toString(out) + ", Expected: " + toString(expected));
				if (getFlag('c')) fullCarry &= test;
				
				a = new byte[] {(byte)0x55, (byte)0xaa};
				b = new byte[] {(byte)0xaa, (byte)0x55};
				out = BAMath.multiply(a, b);
				expected = new byte[] {(byte)0x72, (byte)0x55, (byte)0x55, (byte)0xe3};
				test = equals(out, expected);
				System.out.print("\nTest 9.4: " + (test ? "PASSED":"FAIL"));
				if (getFlag('n')) System.out.print(" - Set 2 Byte Multiplication");
				if (getFlag('e')) System.out.print(" - A: " + toString(a) + ", B: " + toString(b) + ", Out: " + toString(out) + ", Expected: " + toString(expected));
				if (getFlag('c')) fullCarry &= test;
				
				a = new byte[] {(byte)0x55, (byte)0xaa, (byte)0x55}; //little endian
				b = new byte[] {(byte)0xaa, (byte)0x55, (byte)0xaa}; //little endian
				out = BAMath.multiply(a, b);
				expected = new byte[] {(byte)0x72, (byte)0x55, (byte)0xe3, (byte)0x71, (byte)0x55, (byte)0xe3};
				test = equals(out, expected);
				System.out.print("\nTest 9.5: " + (test ? "PASSED":"FAIL"));
				if (getFlag('n')) System.out.print(" - Set 2 Byte Multiplication");
				if (getFlag('e')) System.out.print(" - A: " + toString(a) + ", B: " + toString(b) + ", Out: " + toString(out) + ", Expected: " + toString(expected));
				if (getFlag('c')) fullCarry &= test;
				
				a = new byte[] {(byte)0xaa, (byte)0xaa}; //little endian
				b = new byte[] {(byte)0x55, (byte)0x55}; //little endian
				out = BAMath.multiply(a, b);
				expected = new byte[] {(byte)0x72, (byte)0x1c, (byte)0x8e, (byte)0xE3};
				test = equals(out, expected);
				System.out.print("\nTest 9.6: " + (test ? "PASSED":"FAIL"));
				if (getFlag('n')) System.out.print(" - Negative Multiplication Stay Negative");
				if (getFlag('e')) System.out.print(" - A: " + toString(a) + ", B: " + toString(b) + ", Out: " + toString(out) + ", Expected: " + toString(expected));
				if (getFlag('c')) fullCarry &= test;
				
				a = new byte[] {(byte)0xaa, (byte)0xaa}; //little endian
				b = new byte[] {(byte)0xaa, (byte)0xaa}; //little endian
				out = BAMath.multiply(a, b);
				expected = new byte[] {(byte)0xe4, (byte)0x38, (byte)0x72, (byte)0x1c};
				test = equals(out, expected);
				System.out.print("\nTest 9.7: " + (test ? "PASSED":"FAIL"));
				if (getFlag('n')) System.out.print(" - Negative Multiplication To Positive");
				if (getFlag('e')) System.out.print(" - A: " + toString(a) + ", B: " + toString(b) + ", Out: " + toString(out) + ", Expected: " + toString(expected));
				if (getFlag('c')) fullCarry &= test;
				
				System.out.println();
			}
			
			if (choice == 10 || choice == 100){ //Byte array value comparison (greater than, less than, equal to)
				//Greater than testing
				a = new byte[] {(byte)0b11};
				b = new byte[] {(byte)0b10};
				out = BAMath.greater(a, b);
				expected = a;
				test = equals(out, expected);
				System.out.print("\nTest 10.1: " + (test ? "PASSED":"FAIL"));
				if (getFlag('n')) System.out.print(" - A Greater");
				if (getFlag('e')) System.out.print(" - A: " + toString(a) + ", B: " + toString(b) + ", Out: " + toString(out) + ", Expected: " + toString(expected));
				if (getFlag('c')) fullCarry &= test;
				
				a = new byte[] {(byte)0b10};
				b = new byte[] {(byte)0b11};
				out = BAMath.greater(a, b);
				expected = b;
				test = equals(out, expected);
				System.out.print("\nTest 10.2: " + (test ? "PASSED":"FAIL"));
				if (getFlag('n')) System.out.print(" - B Greater");
				if (getFlag('e')) System.out.print(" - A: " + toString(a) + ", B: " + toString(b) + ", Out: " + toString(out) + ", Expected: " + toString(expected));
				if (getFlag('c')) fullCarry &= test;
				
				a = new byte[] {(byte)0b10};
				b = new byte[] {(byte)0b10};
				out = BAMath.greater(a, b);
				expected = null;
				test = equals(out, expected);
				System.out.print("\nTest 10.3: " + (test ? "PASSED":"FAIL"));
				if (getFlag('n')) System.out.print(" - Neither Greater");
				if (getFlag('e')) System.out.print(" - A: " + toString(a) + ", B: " + toString(b) + ", Out: " + toString(out) + ", Expected: " + toString(expected));
				if (getFlag('c')) fullCarry &= test;
				
				a = new byte[] {(byte)0xa4};
				b = new byte[] {(byte)0x24};
				out = BAMath.greater(a, b);
				expected = b;
				test = equals(out, expected);
				System.out.print("\nTest 10.4: " + (test ? "PASSED":"FAIL"));
				if (getFlag('n')) System.out.print(" - Negative Greater");
				if (getFlag('e')) System.out.print(" - A: " + toString(a) + ", B: " + toString(b) + ", Out: " + toString(out) + ", Expected: " + toString(expected));
				if (getFlag('c')) fullCarry &= test;
				
				//Lesser than testing
				a = new byte[] {(byte)0b01110100};
				b = new byte[] {(byte)0b01111000};
				out = BAMath.lesser(a, b);
				expected = a;
				test = equals(out, expected);
				System.out.print("\nTest 10.5: " + (test ? "PASSED":"FAIL"));
				if (getFlag('n')) System.out.print(" - A Lesser");
				if (getFlag('e')) System.out.print(" - A: " + toString(a) + ", B: " + toString(b) + ", Out: " + toString(out) + ", Expected: " + toString(expected));
				if (getFlag('c')) fullCarry &= test;
				
				a = new byte[] {(byte)0b01110100};
				b = new byte[] {(byte)0b01110000};
				out = BAMath.lesser(a, b);
				expected = b;
				test = equals(out, expected);
				System.out.print("\nTest 10.6: " + (test ? "PASSED":"FAIL"));
				if (getFlag('n')) System.out.print(" - B Lesser");
				if (getFlag('e')) System.out.print(" - A: " + toString(a) + ", B: " + toString(b) + ", Out: " + toString(out) + ", Expected: " + toString(expected));
				if (getFlag('c')) fullCarry &= test;
				
				a = new byte[] {(byte)0b01110100};
				b = new byte[] {(byte)0b01110100};
				out = BAMath.lesser(a, b);
				expected = null;
				test = equals(out, expected);
				System.out.print("\nTest 10.7: " + (test ? "PASSED":"FAIL"));
				if (getFlag('n')) System.out.print(" - Neither Lesser");
				if (getFlag('e')) System.out.print(" - A: " + toString(a) + ", B: " + toString(b) + ", Out: " + toString(out) + ", Expected: " + toString(expected));
				if (getFlag('c')) fullCarry &= test;
				
				a = new byte[] {(byte)0xa4, (byte)0xa4};
				b = new byte[] {(byte)0x24};
				out = BAMath.lesser(a, b);
				expected = a;
				test = equals(out, expected);
				System.out.print("\nTest 10.8: " + (test ? "PASSED":"FAIL"));
				if (getFlag('n')) System.out.print(" - Negative Lesser");
				if (getFlag('e')) System.out.print(" - A: " + toString(a) + ", B: " + toString(b) + ", Out: " + toString(out) + ", Expected: " + toString(expected));
				if (getFlag('c')) fullCarry &= test;
				
				//Equals testing
				a = new byte[] {(byte)0b01110100};
				b = new byte[] {(byte)0b01110100};
				out = BAMath.equals(a, b)?(new byte[]{(byte)1}):(new byte[]{(byte)0});
				expected = new byte[] {(byte)1};
				test = equals(out, expected);
				System.out.print("\nTest 10.9: " + (test ? "PASSED":"FAIL"));
				if (getFlag('n')) System.out.print(" - Both Equals");
				if (getFlag('e')) System.out.print(" - A: " + toString(a) + ", B: " + toString(b) + ", Out: " + toString(out) + ", Expected: " + toString(expected));
				if (getFlag('c')) fullCarry &= test;
				
				a = new byte[] {(byte)0b01110100};
				b = new byte[] {(byte)0b01110000};
				out = BAMath.equals(a, b)?(new byte[]{(byte)1}):(new byte[]{(byte)0});
				expected = new byte[] {(byte)0};
				test = equals(out, expected);
				System.out.print("\nTest 10.A: " + (test ? "PASSED":"FAIL"));
				if (getFlag('n')) System.out.print(" - Not Equals");
				if (getFlag('e')) System.out.print(" - A: " + toString(a) + ", B: " + toString(b) + ", Out: " + toString(out) + ", Expected: " + toString(expected));
				if (getFlag('c')) fullCarry &= test;
				
				
				System.out.println();
			}
			
			if (choice == 11 || choice == 100){ //Byte array division
				byte[] remainder = new byte[1];
				byte[] expectedRemainder= new byte[] {0};
				a = new byte[] {(byte)0b100};
				b = new byte[] {(byte)0b10};
				out = BAMath.divide(a, b, remainder);
				if (getFlag('r')) out = BAMath.divideR(a,b, remainder);
				expected = new byte[] {(byte)0b10};
				test = equals(out, expected) && equals(remainder, expectedRemainder);
				System.out.print("\nTest 11.1: " + (test ? "PASSED":"FAIL"));
				if (getFlag('n')) System.out.print(" - Small Byte Division");
				if (getFlag('e')) System.out.print(" - A: " + toString(a) + ", B: " + toString(b) + ", Out: " + toString(out) + ", Expected: " + toString(expected) + ", Remainder: " + toString(remainder) + ", Expected Remainder: " + toString(expectedRemainder));
				if (getFlag('c')) fullCarry &= test;
				
				remainder = new byte[1];
				expectedRemainder= new byte[] {0};
				a = new byte[] {(byte)0b01000101};
				b = new byte[] {(byte)0b00010111};
				out = BAMath.divide(a, b, remainder);
				if (getFlag('r')) out = BAMath.divideR(a,b, remainder);
				expected = new byte[] {(byte)0b11};
				test = equals(out, expected) && equals(remainder, expectedRemainder);
				System.out.print("\nTest 11.2: " + (test ? "PASSED":"FAIL"));
				if (getFlag('n')) System.out.print(" - Small Byte Division Pt 2");
				if (getFlag('e')) System.out.print(" - A: " + toString(a) + ", B: " + toString(b) + ", Out: " + toString(out) + ", Expected: " + toString(expected) + ", Remainder: " + toString(remainder) + ", Expected Remainder: " + toString(expectedRemainder));
				if (getFlag('c')) fullCarry &= test;
				
				remainder = new byte[2];
				expectedRemainder= new byte[] {0, 0};
				a = new byte[] {(byte)0xec, (byte)0x40, (byte)0xe0, (byte)0x7a};
				b = new byte[] {(byte)0x4e, (byte)0x93};
				out = BAMath.divide(a, b, remainder);
				if (getFlag('r'))out = BAMath.divideR(a,b, remainder);
				expected = new byte[] {(byte)0x9a, (byte)0xde, (byte)0xfe};
				if (getFlag('r')) expected = new byte[] {(byte)0x9a, (byte)0xde, (byte)0xfe, (byte)0xff}; //'R' functions dont have trimmings happen
				test = equals(out, expected) && equals(remainder, expectedRemainder);
				System.out.print("\nTest 11.3: " + (test ? "PASSED":"FAIL"));
				if (getFlag('n')) System.out.print(" - 2 Byte Division");
				if (getFlag('e')) System.out.print(" - A: " + toString(a) + ", B: " + toString(b) + ", Out: " + toString(out) + ", Expected: " + toString(expected) + ", Remainder: " + toString(remainder) + ", Expected Remainder: " + toString(expectedRemainder));
				if (getFlag('c')) fullCarry &= test;
				
				remainder = new byte[2];
				expectedRemainder= new byte[] {(byte)0xdf, 0x33};
				a = new byte[] {(byte)0xcb, (byte)0x74, (byte)0xe0, (byte)0x7a};
				b = new byte[] {(byte)0x4e, (byte)0x93};
				out = BAMath.divide(a, b, remainder);
				if (getFlag('r'))out = BAMath.divideR(a,b, remainder);
				expected = new byte[] {(byte)0x9a, (byte)0xde, (byte)0xfe};
				if (getFlag('r')) expected = new byte[] {(byte)0x9a, (byte)0xde, (byte)0xfe, (byte)0xff}; //'R' functions dont have trimmings happen
				test = equals(out, expected) && equals(remainder, expectedRemainder);
				System.out.print("\nTest 11.4: " + (test ? "PASSED":"FAIL"));
				if (getFlag('n')) System.out.print(" - 2 Byte Division With Remainder");
				if (getFlag('e')) System.out.print(" - A: " + toString(a) + ", B: " + toString(b) + ", Out: " + toString(out) + ", Expected: " + toString(expected) + ", Remainder: " + toString(remainder) + ", Expected Remainder: " + toString(expectedRemainder));
				if (getFlag('c')) fullCarry &= test;
				
				remainder = new byte[2];
				expectedRemainder= new byte[] {4, 0};
				a = new byte[] {(byte)0x36};
				b = new byte[] {(byte)0xa};
				out = BAMath.divide(a, b, remainder);
				if (getFlag('r'))out = BAMath.divideR(a,b, remainder);
				expected = new byte[] {(byte)0x5};
				if (getFlag('r')) expected = new byte[] {(byte)0x5}; //'R' functions dont have trimmings happen
				test = equals(out, expected) && equals(remainder, expectedRemainder);
				System.out.print("\nTest 11.5: " + (test ? "PASSED":"FAIL"));
				if (getFlag('n')) System.out.print(" - Division Problem Set");
				if (getFlag('e')) System.out.print(" - A: " + toString(a) + ", B: " + toString(b) + ", Out: " + toString(out) + ", Expected: " + toString(expected) + ", Remainder: " + toString(remainder) + ", Expected Remainder: " + toString(expectedRemainder));
				if (getFlag('c')) fullCarry &= test;
				
				remainder = new byte[2];
				expectedRemainder= new byte[] {8, 0};
				a = new byte[] {(byte)0xfc, (byte)0xd3};
				b = new byte[] {(byte)0xa};
				out = BAMath.divide(a, b, remainder);
				if (getFlag('r'))out = BAMath.divideR(a,b, remainder);
				expected = new byte[] {(byte)0x9a, (byte)0xfb};
				//if (getFlag('r')) expected = new byte[] {(byte)0x9a, (byte)0xfb, (byte)0xff, (byte)0xff}; //'R' functions dont have trimmings happen
				test = equals(out, expected) && equals(remainder, expectedRemainder);
				System.out.print("\nTest 11.6: " + (test ? "PASSED":"FAIL"));
				if (getFlag('n')) System.out.print(" - Division Problem Set Pt 2");
				if (getFlag('e')) System.out.print(" - A: " + toString(a) + ", B: " + toString(b) + ", Out: " + toString(out) + ", Expected: " + toString(expected) + ", Remainder: " + toString(remainder) + ", Expected Remainder: " + toString(expectedRemainder));
				if (getFlag('c')) fullCarry &= test;
				
				System.out.println();
			}
			
			if (choice == 12 || choice == 100){ //Byte array modulo testing
				a = new byte[] {(byte)0b100};
				b = new byte[] {(byte)0b10};
				out = BAMath.modulo(a, b);
				if (getFlag('r')) out = BAMath.moduloR(a,b);
				expected = new byte[] {(byte)0b0};
				test = equals(out, expected);
				System.out.print("\nTest 12.1: " + (test ? "PASSED":"FAIL"));
				if (getFlag('n')) System.out.print(" - No Remainder");
				if (getFlag('e')) System.out.print(" - A: " + toString(a) + ", B: " + toString(b) + ", Out: " + toString(out) + ", Expected: " + toString(expected));
				if (getFlag('c')) fullCarry &= test;
				
				a = new byte[] {(byte)0xcb, (byte)0x74, (byte)0xe0, (byte)0x7a};
				b = new byte[] {(byte)0x4e, (byte)0x93};
				out = BAMath.modulo(a, b);
				if (getFlag('r'))out = BAMath.moduloR(a,b);
				expected = new byte[] {(byte)0x23};
				if (getFlag('r')) expected = new byte[] {(byte)0x23, (byte)0x0, (byte)0x0, (byte)0x0}; //'R' functions dont have trimmings happen
				test = equals(out, expected);
				System.out.print("\nTest 12.2: " + (test ? "PASSED":"FAIL"));
				if (getFlag('n')) System.out.print(" - Remainder Exists");
				if (getFlag('e')) System.out.print(" - A: " + toString(a) + ", B: " + toString(b) + ", Out: " + toString(out) + ", Expected: " + toString(expected));
				if (getFlag('c')) fullCarry &= test;
				
				System.out.println();
			}
			
			if (choice == 13 || choice == 100){ //byte array Base 9* printing
				a = new byte[] {(byte)7};
				String outString = BAMath.toDecString(a);
				String expectedString  = "7";
				test = outString.equals(expectedString);
				System.out.print("\nTest 13.1: " + (test ? "PASSED":"FAIL"));
				if (getFlag('n')) System.out.print(" - Single Digit - Single Byte");
				if (getFlag('e')) System.out.print(" - A: " + toString(a) + ", Out: " + outString + ", Expected: " + expectedString);
				if (getFlag('c')) fullCarry &= test;
				
				a = new byte[] {(byte)23};
				outString = BAMath.toDecString(a);
				expectedString  = "23";
				test = outString.equals(expectedString);
				System.out.print("\nTest 13.2: " + (test ? "PASSED":"FAIL"));
				if (getFlag('n')) System.out.print(" - Double Digit - Single Byte");
				if (getFlag('e')) System.out.print(" - A: " + toString(a) + ", Out: " + outString + ", Expected: " + expectedString);
				if (getFlag('c')) fullCarry &= test;
				
				a = new byte[] {(byte)101};
				outString = BAMath.toDecString(a);
				expectedString  = "101";
				test = outString.equals(expectedString);
				System.out.print("\nTest 13.3: " + (test ? "PASSED":"FAIL"));
				if (getFlag('n')) System.out.print(" - Triple Digit - Single Byte");
				if (getFlag('e')) System.out.print(" - A: " + toString(a) + ", Out: " + outString + ", Expected: " + expectedString);
				if (getFlag('c')) fullCarry &= test;
				
				a = new byte[] {(byte)0xfc, (byte)0x53};
				outString = BAMath.toDecString(a);
				expectedString  = "21500";
				test = outString.equals(expectedString);
				System.out.print("\nTest 13.4: " + (test ? "PASSED":"FAIL"));
				if (getFlag('n')) System.out.print(" - Multi Digit - Double Byte");
				if (getFlag('e')) System.out.print(" - A: " + toString(a) + ", Out: " + outString + ", Expected: " + expectedString);
				if (getFlag('c')) fullCarry &= test;
				
				a = new byte[] {(byte)0x69, (byte)0xc1, (byte)0x58, (byte)0x20}; //problem between 00,D3,FC nad 1F,FF - should be 15,32
				outString = BAMath.toDecString(a);
				expectedString  = "542687593";
				test = outString.equals(expectedString);
				System.out.print("\nTest 13.5: " + (test ? "PASSED":"FAIL"));
				if (getFlag('n')) System.out.print(" - Multi Digit - Multi Byte");
				if (getFlag('e')) System.out.print(" - A: " + toString(a) + ", Out: " + outString + ", Expected: " + expectedString);
				if (getFlag('c')) fullCarry &= test;
				
				System.out.println();
			}
			
			if (choice == 14 || choice == 100){ //byte array modulo multiplcation
				a = new byte[] {(byte)0xc7, (byte)0x0};
				b = new byte[] {(byte)0x32};
				out = BAMath.moduloMult(a, b, new byte[] {(byte)0x0a});
				expected = new byte[] {(byte)0x0};
				test = equals(out, expected);
				System.out.print("\nTest14.1: " + (test ? "PASSED":"FAIL"));
				if (getFlag('n')) System.out.print(" - Single Byte 0");
				if (getFlag('e')) System.out.print(" - A: " + toString(a) + ", B: " + toString(b) + ", Out: " + toString(out) + ", Expected: " + toString(expected));
				if (getFlag('c')) fullCarry &= test;
				
				System.out.println();
			}
			
			if (getFlag('c')) System.out.println("\n\nAll Passed: " + fullCarry);
			
		}
		
		/* Notes
		 * To accurately portray which base we mean, call them Base (n-1)
		  * Examples:
		  * 0-9 = base 9, 
		  * 0-1 = base 1, 
		  * 0-15 = base 15
		 */
	}
	int flags = 0;
	char[] validFlags;
	
	public boolean equals(byte[] a, byte[] b){
		if (a==null && b==null) return true; //null == null is true
		if (a==null || b==null) return false; //one exists, the other doesnt
		if (a==b) return true; //same arrays, cant be different
		if (a.length != b.length) return false; //different sizes
		for (int i=0;i<a.length;i++){
			if (a[i] != b[i]) return false;
		}
		return true;
	}
	
	//specifc bit setting methods to quickly control which flags are active for the specified prompt
	public void addFlag(char item){
		if (!Character.isLetter(item)) return; //must be a letter
		if (Character.isUpperCase(item)) item = (char)((int)item+32); //force lowercase letters
		for (int i=0;i<validFlags.length;i++){
			if (validFlags[i] == item){
				setFlag((int)item-97, true);
				return;
			}
		}
	}
	public boolean getFlag(char item){
		if (!Character.isLetter(item)) return false; //must be a letter
		if (Character.isUpperCase(item)) item = (char)((int)item+32); //force lowercase letters
		return getFlag((int)item-97);
	}
	
	//general bit settings methods
	public void setFlag(int bitIndex, boolean value){
		if (value){
			flags |= (1<<bitIndex); //flips flag to 1
			return;
		}
		flags &= ~(1<<bitIndex); //flips flag to 0
		return;
	}
	public boolean getFlag(int bitIndex){
		return ((flags >> bitIndex) &0b1) == 1;
	}
	
	public static String toBinaryString(byte val){
		char[] send = new char[8];
		for (int i=7;i>=0;i--){
			send[i] = (char)((val & 0b1) + 48);
			val >>>=1;
		}
		return new String(send);
	}
	public static String toBinaryString(byte[] array){
		if (array == null) return "null";
		char[] send = new char[array.length * 9 -1];
		byte temp = 0;
		for (int n=array.length-1;n>=0;n--){
			temp = array[n];
			for (int i=7;i>=0;i--){
				send[(array.length-1-n)*9+i] = (char)((array[n]&0b1)+48);
				array[n] >>>=1;
			}
			array[n] = temp; //don't muddle with the array
			if (n!=0)send[(array.length-1-n)*9+8] = '|';
		}
		return new String(send);
	}
	public static String toString(byte[] array){
		if (array == null) return "null";
		char[] send = new char[array.length * 3];
		int c=1;
		for (int i=array.length-1;i>=0;i--){
			send[c++] = (char)(((array[i]&0xf0)>>4) + (((array[i]&0xf0)>>4) >= 10 ? 55 : 48));
			send[c++] = (char)((array[i]&0xf) + ((array[i]&0xf) >= 10 ? 55 : 48));
			if (c < send.length) send[c++] = ',';
		}
		return new String(send);
	}
	public static String toString(byte val){
		char[] send = new char[2];
		send[0] = (char)(((val&0xf0)>>4) + (((val&0xf0)>>4) >= 10 ? 55 : 48));
		send[1] = (char)((val&0xf) + ((val&0xf) >= 10 ? 55 : 48));
		return new String(send);
	}
}