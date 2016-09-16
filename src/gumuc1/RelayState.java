package gumuc1;

public enum RelayState{
	UNDEFINED,
	LIGHT_ON,
	LIGHT_OFF;
	
	public static RelayState fromBoolean(boolean val){
		return (val)?LIGHT_ON:LIGHT_OFF;
	}
}