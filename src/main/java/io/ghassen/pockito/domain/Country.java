package io.ghassen.pockito.domain;

/**
 * Country enum following ISO 3166-1 standard.
 * 
 * Decision: We use alpha-2 codes (2-letter) as the primary representation because:
 * 1. They are more widely recognized and used in most APIs and systems
 * 2. They are shorter and more efficient for storage and transmission
 * 3. They are the most commonly used format in international standards
 * 
 * Alpha-3 codes (3-letter) are provided as an alternative for systems that require them.
 * 
 * @see <a href="https://en.wikipedia.org/wiki/ISO_3166-1">ISO 3166-1</a>
 */
public enum Country {
    // Major countries
    US("US", "USA", "United States"),
    CA("CA", "CAN", "Canada"),
    GB("GB", "GBR", "United Kingdom"),
    DE("DE", "DEU", "Germany"),
    FR("FR", "FRA", "France"),
    IT("IT", "ITA", "Italy"),
    ES("ES", "ESP", "Spain"),
    NL("NL", "NLD", "Netherlands"),
    BE("BE", "BEL", "Belgium"),
    CH("CH", "CHE", "Switzerland"),
    AT("AT", "AUT", "Austria"),
    SE("SE", "SWE", "Sweden"),
    NO("NO", "NOR", "Norway"),
    DK("DK", "DNK", "Denmark"),
    FI("FI", "FIN", "Finland"),
    PL("PL", "POL", "Poland"),
    CZ("CZ", "CZE", "Czech Republic"),
    HU("HU", "HUN", "Hungary"),
    RO("RO", "ROU", "Romania"),
    BG("BG", "BGR", "Bulgaria"),
    HR("HR", "HRV", "Croatia"),
    SI("SI", "SVN", "Slovenia"),
    SK("SK", "SVK", "Slovakia"),
    LT("LT", "LTU", "Lithuania"),
    LV("LV", "LVA", "Latvia"),
    EE("EE", "EST", "Estonia"),
    IE("IE", "IRL", "Ireland"),
    PT("PT", "PRT", "Portugal"),
    GR("GR", "GRC", "Greece"),
    CY("CY", "CYP", "Cyprus"),
    MT("MT", "MLT", "Malta"),
    LU("LU", "LUX", "Luxembourg"),
    
    // Asia-Pacific
    JP("JP", "JPN", "Japan"),
    CN("CN", "CHN", "China"),
    KR("KR", "KOR", "South Korea"),
    IN("IN", "IND", "India"),
    AU("AU", "AUS", "Australia"),
    NZ("NZ", "NZL", "New Zealand"),
    SG("SG", "SGP", "Singapore"),
    HK("HK", "HKG", "Hong Kong"),
    TW("TW", "TWN", "Taiwan"),
    TH("TH", "THA", "Thailand"),
    MY("MY", "MYS", "Malaysia"),
    ID("ID", "IDN", "Indonesia"),
    PH("PH", "PHL", "Philippines"),
    VN("VN", "VNM", "Vietnam"),
    
    // Americas
    MX("MX", "MEX", "Mexico"),
    BR("BR", "BRA", "Brazil"),
    AR("AR", "ARG", "Argentina"),
    CL("CL", "CHL", "Chile"),
    CO("CO", "COL", "Colombia"),
    PE("PE", "PER", "Peru"),
    VE("VE", "VEN", "Venezuela"),
    UY("UY", "URY", "Uruguay"),
    PY("PY", "PRY", "Paraguay"),
    BO("BO", "BOL", "Bolivia"),
    EC("EC", "ECU", "Ecuador"),
    GT("GT", "GTM", "Guatemala"),
    HN("HN", "HND", "Honduras"),
    SV("SV", "SLV", "El Salvador"),
    NI("NI", "NIC", "Nicaragua"),
    CR("CR", "CRI", "Costa Rica"),
    PA("PA", "PAN", "Panama"),
    
    // Middle East & Africa
    IL("IL", "ISR", "Israel"),
    AE("AE", "ARE", "United Arab Emirates"),
    SA("SA", "SAU", "Saudi Arabia"),
    TR("TR", "TUR", "Turkey"),
    EG("EG", "EGY", "Egypt"),
    ZA("ZA", "ZAF", "South Africa"),
    NG("NG", "NGA", "Nigeria"),
    KE("KE", "KEN", "Kenya"),
    MA("MA", "MAR", "Morocco"),
    TN("TN", "TUN", "Tunisia"),
    DZ("DZ", "DZA", "Algeria"),
    LY("LY", "LBY", "Libya"),
    SD("SD", "SDN", "Sudan"),
    ET("ET", "ETH", "Ethiopia"),
    GH("GH", "GHA", "Ghana"),
    CI("CI", "CIV", "Ivory Coast"),
    SN("SN", "SEN", "Senegal"),
    
    // Europe (additional)
    RU("RU", "RUS", "Russia"),
    UA("UA", "UKR", "Ukraine"),
    BY("BY", "BLR", "Belarus"),
    MD("MD", "MDA", "Moldova"),
    RS("RS", "SRB", "Serbia"),
    ME("ME", "MNE", "Montenegro"),
    BA("BA", "BIH", "Bosnia and Herzegovina"),
    MK("MK", "MKD", "North Macedonia"),
    AL("AL", "ALB", "Albania"),
    XK("XK", "XKX", "Kosovo"),
    
    // Other notable countries
    IS("IS", "ISL", "Iceland"),
    GL("GL", "GRL", "Greenland"),
    FO("FO", "FRO", "Faroe Islands"),
    AD("AD", "AND", "Andorra"),
    MC("MC", "MCO", "Monaco"),
    LI("LI", "LIE", "Liechtenstein"),
    SM("SM", "SMR", "San Marino"),
    VA("VA", "VAT", "Vatican City");

    private final String alpha2Code;
    private final String alpha3Code;
    private final String name;

    Country(String alpha2Code, String alpha3Code, String name) {
        this.alpha2Code = alpha2Code;
        this.alpha3Code = alpha3Code;
        this.name = name;
    }

    public String getAlpha2Code() {
        return alpha2Code;
    }

    public String getAlpha3Code() {
        return alpha3Code;
    }

    public String getName() {
        return name;
    }

    /**
     * Find country by alpha-2 code
     */
    public static Country fromAlpha2Code(String alpha2Code) {
        if (alpha2Code == null) {
            return null;
        }
        for (Country country : values()) {
            if (country.alpha2Code.equalsIgnoreCase(alpha2Code)) {
                return country;
            }
        }
        throw new IllegalArgumentException("Unknown alpha-2 country code: " + alpha2Code);
    }

    /**
     * Find country by alpha-3 code
     */
    public static Country fromAlpha3Code(String alpha3Code) {
        if (alpha3Code == null) {
            return null;
        }
        for (Country country : values()) {
            if (country.alpha3Code.equalsIgnoreCase(alpha3Code)) {
                return country;
            }
        }
        throw new IllegalArgumentException("Unknown alpha-3 country code: " + alpha3Code);
    }

    /**
     * Find country by either alpha-2 or alpha-3 code
     */
    public static Country fromCode(String code) {
        if (code == null) {
            return null;
        }
        if (code.length() == 2) {
            return fromAlpha2Code(code);
        } else if (code.length() == 3) {
            return fromAlpha3Code(code);
        }
        throw new IllegalArgumentException("Invalid country code length: " + code + ". Must be 2 or 3 characters.");
    }

    @Override
    public String toString() {
        return alpha2Code;
    }
}
