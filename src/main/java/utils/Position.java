package utils;

import jason.util.Pair;

public class Position {

	private int x;
	private int y;
	
	public Position()
	{
		this(0, 0);
	}
	
	public Position(Integer x, Integer y) {
		this.x = x;
		this.y = y;
	}
	
	public int getX() {
		return x;
	}
	
	public int getY() {
		return y;
	}
	
	public void add(Position p)
	{
		if(p == null)
		{
			System.err.println("Position Class: Failed to add positions: p is null");
			return;
		}
		
		this.x += p.x;
		this.y += p.y;
		
	}
	
	@Override
	public String toString()
	{
		return "[X: " + getX() + ", Y: " + getY() + "]";
	}
	
	@Override
	public boolean equals(Object other)
	{
		if(this == other)
			return true;
		
		if(!(other instanceof Position))
			return false;
		
		Position otherPos = (Position) other;
		return this.x == otherPos.x && this.y == otherPos.y;
	}
}
