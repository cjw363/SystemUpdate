package tecsun.cjw.systemupdate.been;

public class SystemInfo {
  private String name;
  private String address;
  private String description;
  private String password;
  private String hwsupport;
  private String formemory;
  private String buildfor;

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getAddress() {
    return address;
  }

  public void setAddress(String address) {
    this.address = address;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public String getPassword() {
    return password;
  }

  public void setPassword(String password) {
    this.password = password;
  }

  public String getHwsupport() {
    return hwsupport;
  }

  public void setHwsupport(String hwsupport) {
    this.hwsupport = hwsupport;
  }

  public String getFormemory() {
    return formemory;
  }

  public void setFormemory(String formemory) {
    this.formemory = formemory;
  }

  public String getBuildfor() {
    return buildfor;
  }

  public void setBuildfor(String buildfor) {
    this.buildfor = buildfor;
  }
}
