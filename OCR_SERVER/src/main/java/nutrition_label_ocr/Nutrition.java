package nutrition_label_ocr;

public class Nutrition {

	
	
	private String value;
	private double match_strength = 0;
	private String unit;
	
	public Nutrition() {
		this.match_strength = 0;
	}

	public String getValue() {
		return value;
	}
	public void setValue(String value) {
		this.value = value.trim();
	}
	public double getMatch_strength() {
		return match_strength;
	}

	public void setMatch_strength(double match_strength) {
		this.match_strength = match_strength;
	}

	public String getUnit() {
		return unit;
	}

	public void setUnit(String unit) {
		this.unit = unit.trim();
	}

	
	
	
	
	
}
