package helpful;

public class Math{
	public static int max(int[] nums){
		int max = nums[0];
		for (int num : nums){
			if (num > max) max = num;
		}
		return max;
	}
	public static int max(int num1, int num2){
		if (num1 > num2) return num1;
		return num2;
	}
	public static int min(int[] nums){
		int min = nums[0];
		for (int num : nums){
			if (num < min) min = num;
		}
		return min;
	}
	public static int min(int num1, int num2){
		if (num1 < num2) return num1;
		return num2;
	}
	public static double abs(double num){
		if (num > 0) return num;
		return num*-1;
	}
}