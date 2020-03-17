package map;


import java.util.HashSet;
import java.util.Objects;

public class Position {

	public final static Position ZERO = new Position(0, 0);

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

	public Position add(Position p)
	{
		if(p == null)
		{
			System.err.println("Position Class: Failed to add positions: p is null");
			return this;
		}

		return new Position(this.x + p.x, this.y + p.y);
	}

	public Position getUnitVector()
	{
		int magnitude = (int) Math.sqrt(Math.pow(x, 2) + Math.pow(y, 2));
		return new Position(x / magnitude, y / magnitude);
	}

	public Position subtract(Position p)
	{
		if(p == null)
		{
			System.err.println("Position Class: Failed to subtract positions: p is null");
			return this;
		}

		return new Position(this.x - p.x, this.y - p.y);
	}

	public Position negate()
	{
		return new Position(-this.x, -this.y);
	}

	public Position clone()
	{
		return new Position(x, y);
	}

	public double getDistance() {
		return Math.sqrt(Math.pow(x, 2) + Math.pow(y, 2));
	}

	public Position multiply(int edgeVision) {
		return new Position(edgeVision * getX(), edgeVision * getY());
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

		if(other instanceof Direction)
			return this.equals((Direction) other);

		if(!(other instanceof Position))
			return false;

		Position otherPos = (Position) other;
		return this.x == otherPos.x && this.y == otherPos.y;
	}

	public boolean equals(Direction other)
	{
		if(other == null)
			return false;

		return this.equals(other.getPosition());
	}

	private String getUniqueHashString()
	{
		return x + "," + y;
	}

	@Override
	public int hashCode() {
		return Objects.hash(getUniqueHashString());
	}

	public boolean isZeroPosition()
	{
		return this.equals(ZERO);
	}

}
