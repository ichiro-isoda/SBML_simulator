package sbml;

class Stack{
	
	private int tail;
	private double[] data = new double[100];
	
	
	public Stack() {
		this.tail = -1;
	}
	
	public void Init() {
		this.tail = -1;
	}
	
	
	public void Push(double input) {
		
		if(this.tail >= 100 - 1) {
			System.out.println("ERROR:STACK IS FULL!!");
			return;
		}
		
		this.data[this.tail + 1] = input;
		this.tail = this.tail + 1;
	}
	
	
	public double Pop() {
		double ret = 0;
		if(this.tail == -1) {
			System.out.println("ERROR:STACK IS EMPTY!!");
			ret = -1;
			return ret;
		}
		
		ret = this.data[this.tail];
		this.tail = this.tail - 1;
		return ret;
	}
	
	
	void CheckStack() {
		//System.out.println("tail = "+this.tail);
		
		for( int i = 0; i <= this.tail; i ++ ) {
			System.out.println(this.data[ this.tail - i ]);
		}
		System.out.println();
	}
	
	
}