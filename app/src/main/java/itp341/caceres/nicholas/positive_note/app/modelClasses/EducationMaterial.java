package itp341.caceres.nicholas.positive_note.app.modelClasses;

/**
 * Created by NLCaceres on 5/3/2016.
 */
public class EducationMaterial {

  private String SectionTitle;
  private String SectionFirstURLTitle;
  private String SectionFirstURL;
  private String SectionSecondURLTitle;
  private String SectionSecondURL;
  private String SectionThirdURLTitle;
  private String SectionThirdURL;

  // Class will be primarily for reading values into textviews
  // Only creating for easy storage and access of educational material

  public EducationMaterial() {
    SectionTitle = "";
    SectionFirstURL = "";
    SectionSecondURL = "";
    SectionThirdURL = "";
  }

  public EducationMaterial(String sectionTitle, String sectionFirstURLTitle, String sectionSecondURLTitle, String sectionThirdURLTitle) {
    this.SectionTitle = sectionTitle;
    this.SectionFirstURLTitle = sectionFirstURLTitle;
    this.SectionSecondURLTitle = sectionSecondURLTitle;
    this.SectionThirdURLTitle = sectionThirdURLTitle;
  }

  public EducationMaterial(String sectionTitle, String sectionFirstURLTitle, String sectionFirstURL, String sectionSecondURLTitle, String sectionSecondURL, String sectionThirdURLTitle, String sectionThirdURL) {
    this.SectionTitle = sectionTitle;
    this.SectionFirstURLTitle = sectionFirstURLTitle;
    this.SectionFirstURL = sectionFirstURL;
    this.SectionSecondURLTitle = sectionSecondURLTitle;
    this.SectionSecondURL = sectionSecondURL;
    this.SectionThirdURLTitle = sectionThirdURLTitle;
    this.SectionThirdURL = sectionThirdURL;
  }

  public String getSectionTitle() {
    return SectionTitle;
  }

  public void setSectionTitle(String sectionTitle) {
    SectionTitle = sectionTitle;
  }

  public String getSectionFirstURL() {
    return SectionFirstURL;
  }

  public void setSectionFirstURL(String sectionFirstURL) {
    SectionFirstURL = sectionFirstURL;
  }

  public String getSectionSecondURL() {
    return SectionSecondURL;
  }

  public void setSectionSecondURL(String sectionSecondURL) {
    SectionSecondURL = sectionSecondURL;
  }

  public String getSectionThirdURL() {
    return SectionThirdURL;
  }

  public void setSectionThirdURL(String sectionThirdURL) {
    SectionThirdURL = sectionThirdURL;
  }

  public String getSectionFirstURLTitle() {
    return SectionFirstURLTitle;
  }

  public void setSectionFirstURLTitle(String sectionFirstURLTitle) {
    SectionFirstURLTitle = sectionFirstURLTitle;
  }

  public String getSectionSecondURLTitle() {
    return SectionSecondURLTitle;
  }

  public void setSectionSecondURLTitle(String sectionSecondURLTitle) {
    SectionSecondURLTitle = sectionSecondURLTitle;
  }

  public String getSectionThirdURLTitle() {
    return SectionThirdURLTitle;
  }

  public void setSectionThirdURLTitle(String sectionThirdURLTitle) {
    SectionThirdURLTitle = sectionThirdURLTitle;
  }
}
