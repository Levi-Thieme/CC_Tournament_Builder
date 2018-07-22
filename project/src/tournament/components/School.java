package tournament.components;

/**
 * School represents a school and contains relevant data fields.
 * @author CS360 Team 5
 *
 */
public class School {
	
	private int id;
	private String name;
	private String address;
	private String city;
	private String zip;
	private boolean hasBoysTeam;
	private boolean hasGirlsTeam;
	private int size;
	private double longitude;
	private double latitude;
	
	
	/** School Data Object **
	 * 
	 * @param ID - Database ID of the school (PK)
	 * @param name - name of the school
	 * @param addr - Address of the school (number and street)
	 * @param city - city school resides in 
	 * @param zip - zip code
	 * @param canHost - School is able to host a track event
	 * @param hasBoysTeam - the school has a boys team
	 * @param hasGirlsTeam - the school has a girls team
	 * @param size - Number of students enrolled
	 * @param longitude - longitude of the schools location
	 * @param latitude - latitude of the schools location
	 */
	public School(String name, String addr, String city, String zip, boolean hasBoysTeam, boolean hasGirlsTeam, int size, double longitude, double latitude) {
		this.name = name;
		this.address = addr;
		this.size = size;
		this.longitude = longitude;
		this.latitude = latitude;
		this.zip = zip;
		this.city = city;
		this.hasBoysTeam = hasBoysTeam;
		this.hasGirlsTeam = hasGirlsTeam;
	}
	
	public School(int id, String name, String addr, String city, String zip, boolean hasBoysTeam, boolean hasGirlsTeam, int size, double longitude, double latitude) {
		this.id = id;
		this.name = name;
		this.address = addr;
		this.size = size;
		this.longitude = longitude;
		this.latitude = latitude;
		this.zip = zip;
		this.city = city;
		this.hasBoysTeam = hasBoysTeam;
		this.hasGirlsTeam = hasGirlsTeam;
	}
	
	public boolean equals(Object o) {
		if(o instanceof School && this.id == ((School) o).getId()) {
			return true;
		}
		return false;
	}
	
	
	public School(int id) {
		this.id = id;
	}
	
	/**
	 * 
	 * @param dest Destination school from this
	 * @return The distance in miles from this to dest
	 */
	public double distanceTo(School dest) {
		return 0;
	}


	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}


	/**
	 * @param name the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}


	/**
	 * @return the address
	 */
	public String getAddress() {
		return address;
	}


	/**
	 * @param address the address to set
	 */
	public void setAddress(String address) {
		this.address = address;
	}


	/**
	 * @return the size
	 */
	public int getSize() {
		return size;
	}


	/**
	 * @param size the size to set
	 */
	public void setSize(int size) {
		this.size = size;
	}


	/**
	 * @return the longitude
	 */
	public double getLongitude() {
		return longitude;
	}


	/**
	 * @param longitude the longitude to set
	 */
	public void setLongitude(double longitude) {
		this.longitude = longitude;
	}


	/**
	 * @return the latitude
	 */
	public double getLatitude() {
		return latitude;
	}


	/**
	 * @param latitude the latitude to set
	 */
	public void setLatitude(double latitude) {
		this.latitude = latitude;
	}
	
	public int getId() {
		return id;
	}
	
	public void setId(int id) {
		this.id = id;
	}

	public String getCity() {
		return city;
	}

	public void setCity(String city) {
		this.city = city;
	}

	public String getZip() {
		return zip;
	}

	public void setZip(String zip) {
		this.zip = zip;
	}

	public boolean hasBoysTeam() {
		return hasBoysTeam;
	}

	public void setHasBoysTeam(boolean hasBoysTeam) {
		this.hasBoysTeam = hasBoysTeam;
	}

	public boolean hasGirlsTeam() {
		return hasGirlsTeam;
	}

	public void setHasGirlsTeam(boolean hasGirlsTeam) {
		this.hasGirlsTeam = hasGirlsTeam;
	}

}
