public final class GlobalVars{
	public static final int INSTANT_AFFECT_UTILILITY = 0,
							FOUR_PLAYER_SIX_POINT_TOTAL = 1,
							SELLING_CARD_HALF_PRICE = 2,
							ALL_DAYBREAKS_ACTIVE = 3,
							ELEMENTALS_ATTACK_ONCE = 4,
							SHIELD_REMOVED_AFTER_ANY_DAMAGE = 5,
							ELEMENTAL_PERMA_DEATH = 6,
							DEATH_MARCH_REQUIRED = 7;
	
	//Magic number - idk how to expand it so above values are setting this up
	//In short: all above settings are 'on' except index 0, instant actions on default dont affect utility commands
	public static int settings = 0b11111110;
	
	public static int setBit(int index, int flag, boolean value){
		if (value) return flag | (1 << index);
		return flag & ~(1<<index);
	}
	public static boolean getBit(int index, int flag){
		return ((flag >>> index) & 1) == 1;
	}
}