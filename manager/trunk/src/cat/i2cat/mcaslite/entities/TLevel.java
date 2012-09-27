package cat.i2cat.mcaslite.entities;

public class TLevel {

	private String name;
	private int[] screen;
	private int vBitrate;
	private int aChannels;
	private int aBitrate;
	
	public int[] getScreen() {
		return screen;
	}
	
	public void setScreen(int[] screen) {
		this.screen = screen;
	}
	
	public int getvBitrate() {
		return vBitrate;
	}
	
	public void setvBitrate(int vBitrate) {
		this.vBitrate = vBitrate;
	}
	
	public int getaChannels() {
		return aChannels;
	}
	
	public void setaChannels(int aChannels) {
		this.aChannels = aChannels;
	}
	
	public int getaBitrate() {
		return aBitrate;
	}
	
	public void setaBitrate(int aBitrate) {
		this.aBitrate = aBitrate;
	}
	
	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		this.name = name;
	}
}
