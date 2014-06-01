package cuchaz.cubicChunks.util;

/**
 * Position of a cube.
 * <p>
 * Tall Worlds uses a column coordinate system (which is really just a cube 
 * coordinate system without the y-coordinate), a cube coordinate system,
 * and two block coordinate systems, a cube-relative system, and a world absolute
 * system.
 * <p>
 * It is important that the systems are kept separate. This class should be 
 * used whenever a cube coordinate is passed along, so that it is clear that
 * cube coordinates are being used, and not block coordinates.
 * <p>
 * Additionally, I (Nick) like to use xRel, yRel, and zRel for the relative
 * position of a block inside of a cube. In world space, I (Nick) refer to the
 * coordinates as xAbs, yAbs, and zAbs.
 * <p>
 * See {@link AddressTools} for details of hashing the cube coordinates for keys and 
 * storage.
 * <p>
 * This class also contains some helper methods to switch from/to block
 * coordinates.
 */
public class CubeCoords
{
	private static final int CUBE_MAX_X = 16;
	private static final int CUBE_MAX_Y = 16;
	private static final int CUBE_MAX_Z = 16;
	
	private static final int HALF_CUBE_MAX_X = CUBE_MAX_X / 2;
	private static final int HALF_CUBE_MAX_Y = CUBE_MAX_Y / 2;
	private static final int HALF_CUBE_MAX_Z = CUBE_MAX_Z / 2;
	
	private final int cubeX;
	private final int cubeY;
	private final int cubeZ;
	
	protected CubeCoords( int cubeX, int cubeY, int cubeZ)
	{
		this.cubeX = cubeX;
		this.cubeY = cubeY;
		this.cubeZ = cubeZ;
	}
	
	/**
	 * Gets the x position of the cube in the world.
	 * 
	 * @return The x position.
	 */
	public int getCubeX()
	{
		return this.cubeX;
	}
	
	/**
	 * Gets the y position of the cube in the world.
	 * 
	 * @return The y position.
	 */
	public int getCubeY()
	{
		return this.cubeY;
	}
	
	/**
	 * Gets the z position of the cube in the world.
	 * 
	 * @return The z position.
	 */
	public int getCubeZ()
	{
		return this.cubeZ;
	}
	
	/**
	 * Gets the coordinates of the cube as a string.
	 * 
	 * @return The coordinates, formatted as a string.
	 */
	@Override
	public String toString()
	{
		return this.cubeX + "," + this.cubeY + "," + this.cubeZ;
	}
	
	/**
	 * Compares the CubeCoordinate against the given object.
	 * 
	 * @return True if the cube matches the given object, but false if it
	 * doesn't match, or is null, or not a CubeCoordinate object.
	 */
	@Override
	public boolean equals(Object otherObject)
	{
		if (otherObject == this)
        {
            return true;
        }
		
        if (otherObject == null)
        {
            return false;
        }
        
        if (!(otherObject instanceof Coords))
        {
            return false;
        }
        
        CubeCoords otherCubeCoordinate = (CubeCoords) otherObject;
        
        if (otherCubeCoordinate.cubeX != cubeX)
        {
            return false;
        }
        
        if (otherCubeCoordinate.cubeY != cubeY)
        {
            return false;
        }
        
        if (otherCubeCoordinate.cubeZ != cubeZ)
        {
            return false;
        }
        
        return true;
	}
	
	/**
	 * Gets the absolute position of the cube's center on the x axis.
	 * 
	 * @return The x center of the cube.
	 */
	public int getXCenter()
	{
		return cubeX * CUBE_MAX_X + HALF_CUBE_MAX_X;
	}
	
	/**
	 * Gets the absolute position of the cube's center on the y axis.
	 * 
	 * @return The y center of the cube.
	 */
	public int getYCenter()
	{
		return cubeY * CUBE_MAX_Y + HALF_CUBE_MAX_Y;
	}
	
	/**
	 * Gets the absolute position of the cube's center on the z axis.
	 * 
	 * @return The z center of the cube.
	 */
	public int getZCenter()
	{
		return cubeZ * CUBE_MAX_Z + HALF_CUBE_MAX_Z;
	}
	
	public int getMinBlockX()
	{
		return Coords.cubeToMinBlock(cubeX);
	}
	
	public int getMinBlockY()
	{
		return Coords.cubeToMinBlock(cubeX);
	}
	
	public int getMinBlockZ()
	{
		return Coords.cubeToMinBlock(cubeX);
	}
}
