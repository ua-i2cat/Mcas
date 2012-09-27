package cat.i2cat.mcaslite.entities;

public class TProfile {

	private String name; 
	private String format;
	private String vCodec;
	private String aCodec;
	
	public String getFormat() {
		return format;
	}
	
	public void setFormat(String format) {
		this.format = format;
	}
	
	public String getvCodec() {
		return vCodec;
	}
	
	public void setvCodec(String vCodec) {
		this.vCodec = vCodec;
	}
	
	public String getaCodec() {
		return aCodec;
	}
	
	public void setaCodec(String aCodec) {
		this.aCodec = aCodec;
	}
	
	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		this.name = name;
	}
}
