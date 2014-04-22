package aws

trait UsesPrefix {
  val prefix: String = System.getProperty("farmbot.awsPrefix", "")

  def build(name: String) : String = {
    return prefix + name;
  }
}
