package comparison.distance;

public class IdentityDistance implements Distance<Object> {
	
	double maxDist;
	
	public IdentityDistance() {
		maxDist = 1d;
	}
	
	public IdentityDistance(double maxDist) {
		this.maxDist = maxDist;
	}

	@Override
	public double compute(Object g1, Object g2) {
		return (g1.equals(g2)) ? 0d : maxDist;
	}
	
	public double getMaxDist() {
		return maxDist;
	}

}
