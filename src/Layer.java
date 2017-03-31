public class Layer implements Comparable<Layer> {
	public final String type;
	public final int depthTop;
	public final int depthBot;
	public final boolean isDeviated;

	public Layer(String type, int depthTop, int depthBot, boolean isDeviated) {
		this.type = type;
		this.depthTop = depthTop;
		this.depthBot = depthBot;
		this.isDeviated = isDeviated;
	}

	// compares which layer is deeper
	public int compareTo(Layer that) {
		if (this.depthTop > 0 && that.depthTop > 0)
			return this.depthTop - that.depthTop;
		if (this.depthBot > 0 && that.depthBot > 0)
			return this.depthBot - that.depthBot;
		if (this.depthTop > 0 && that.depthBot > 0) {
			if (this.depthTop == that.depthBot)
				return -1;
			return this.depthTop - that.depthBot;
		}
		if (this.depthBot > 0 && that.depthTop > 0) {
			if (this.depthBot == that.depthTop)
				return 1;
			return this.depthBot - that.depthTop;
		}
		return 0;
	}

	public String toString() {
		String valTop = "" + depthTop;
		String valBot = "" + depthBot;
		if (depthTop < 0) {
			valTop = "unknown";
		}
		if (depthBot < 0) {
			valBot = "unknown";
		}
		return "type: " + type + " depthTop: " + valTop + " depthBot: " + valBot;
	}
}