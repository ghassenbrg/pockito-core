package io.ghassen.pockito.domain.enums;

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
    TH("TH", "THA", "Thailand"),
    MY("MY", "MYS", "Malaysia"),
    ID("ID", "IDN", "Indonesia"),
    PH("PH", "PHL", "Philippines"),
    VN("VN", "VNM", "Vietnam"),
    PK("PK", "PAK", "Pakistan"),
    BD("BD", "BGD", "Bangladesh"),
    LK("LK", "LKA", "Sri Lanka"),
    NP("NP", "NPL", "Nepal"),
    MM("MM", "MMR", "Myanmar"),
    KH("KH", "KHM", "Cambodia"),
    LA("LA", "LAO", "Laos"),
    BN("BN", "BRN", "Brunei"),
    MN("MN", "MNG", "Mongolia"),
    KZ("KZ", "KAZ", "Kazakhstan"),
    UZ("UZ", "UZB", "Uzbekistan"),
    KG("KG", "KGZ", "Kyrgyzstan"),
    TJ("TJ", "TJK", "Tajikistan"),
    TM("TM", "TKM", "Turkmenistan"),
    AF("AF", "AFG", "Afghanistan"),
    IR("IR", "IRN", "Iran"),
    IQ("IQ", "IRQ", "Iraq"),
    SY("SY", "SYR", "Syria"),
    LB("LB", "LBN", "Lebanon"),
    JO("JO", "JOR", "Jordan"),
    PS("PS", "PSE", "Palestine"),
    SA("SA", "SAU", "Saudi Arabia"),
    AE("AE", "ARE", "United Arab Emirates"),
    QA("QA", "QAT", "Qatar"),
    BH("BH", "BHR", "Bahrain"),
    KW("KW", "KWT", "Kuwait"),
    OM("OM", "OMN", "Oman"),
    YE("YE", "YEM", "Yemen"),
    
    // Africa
    EG("EG", "EGY", "Egypt"),
    LY("LY", "LBY", "Libya"),
    TN("TN", "TUN", "Tunisia"),
    DZ("DZ", "DZA", "Algeria"),
    MA("MA", "MAR", "Morocco"),
    SD("SD", "SDN", "Sudan"),
    SS("SS", "SSD", "South Sudan"),
    ET("ET", "ETH", "Ethiopia"),
    ER("ER", "ERI", "Eritrea"),
    DJ("DJ", "DJI", "Djibouti"),
    SO("SO", "SOM", "Somalia"),
    KE("KE", "KEN", "Kenya"),
    UG("UG", "UGA", "Uganda"),
    TZ("TZ", "TZA", "Tanzania"),
    RW("RW", "RWA", "Rwanda"),
    BI("BI", "BDI", "Burundi"),
    CD("CD", "COD", "Democratic Republic of the Congo"),
    CG("CG", "COG", "Republic of the Congo"),
    CF("CF", "CAF", "Central African Republic"),
    TD("TD", "TCD", "Chad"),
    CM("CM", "CMR", "Cameroon"),
    NG("NG", "NGA", "Nigeria"),
    NE("NE", "NER", "Niger"),
    ML("ML", "MLI", "Mali"),
    BF("BF", "BFA", "Burkina Faso"),
    CI("CI", "CIV", "Ivory Coast"),
    GH("GH", "GHA", "Ghana"),
    TG("TG", "TGO", "Togo"),
    BJ("BJ", "BEN", "Benin"),
    SN("SN", "SEN", "Senegal"),
    GM("GM", "GMB", "Gambia"),
    GN("GN", "GIN", "Guinea"),
    GW("GW", "GNB", "Guinea-Bissau"),
    SL("SL", "SLE", "Sierra Leone"),
    LR("LR", "LBR", "Liberia"),
    MR("MR", "MRT", "Mauritania"),
    ZA("ZA", "ZAF", "South Africa"),
    NA("NA", "NAM", "Namibia"),
    BW("BW", "BWA", "Botswana"),
    ZW("ZW", "ZWE", "Zimbabwe"),
    ZM("ZM", "ZMB", "Zambia"),
    MW("MW", "MWI", "Malawi"),
    MZ("MZ", "MOZ", "Mozambique"),
    MG("MG", "MDG", "Madagascar"),
    MU("MU", "MUS", "Mauritius"),
    SC("SC", "SYC", "Seychelles"),
    KM("KM", "COM", "Comoros"),
    ST("ST", "STP", "São Tomé and Príncipe"),
    CV("CV", "CPV", "Cape Verde"),
    AO("AO", "AGO", "Angola"),
    GA("GA", "GAB", "Gabon"),
    GQ("GQ", "GNQ", "Equatorial Guinea"),
    LS("LS", "LSO", "Lesotho"),
    SZ("SZ", "SWZ", "Eswatini"),
    
    // Americas
    BR("BR", "BRA", "Brazil"),
    AR("AR", "ARG", "Argentina"),
    CL("CL", "CHL", "Chile"),
    CO("CO", "COL", "Colombia"),
    PE("PE", "PER", "Peru"),
    UY("UY", "URY", "Uruguay"),
    PY("PY", "PRY", "Paraguay"),
    BO("BO", "BOL", "Bolivia"),
    EC("EC", "ECU", "Ecuador"),
    VE("VE", "VEN", "Venezuela"),
    GY("GY", "GUY", "Guyana"),
    SR("SR", "SUR", "Suriname"),
    GF("GF", "GUF", "French Guiana"),
    MX("MX", "MEX", "Mexico"),
    GT("GT", "GTM", "Guatemala"),
    BZ("BZ", "BLZ", "Belize"),
    SV("SV", "SLV", "El Salvador"),
    HN("HN", "HND", "Honduras"),
    NI("NI", "NIC", "Nicaragua"),
    CR("CR", "CRI", "Costa Rica"),
    PA("PA", "PAN", "Panama"),
    CU("CU", "CUB", "Cuba"),
    JM("JM", "JAM", "Jamaica"),
    HT("HT", "HTI", "Haiti"),
    DO("DO", "DOM", "Dominican Republic"),
    PR("PR", "PRI", "Puerto Rico"),
    TT("TT", "TTO", "Trinidad and Tobago"),
    BB("BB", "BRB", "Barbados"),
    LC("LC", "LCA", "Saint Lucia"),
    VC("VC", "VCT", "Saint Vincent and the Grenadines"),
    GD("GD", "GRD", "Grenada"),
    AG("AG", "ATG", "Antigua and Barbuda"),
    KN("KN", "KNA", "Saint Kitts and Nevis"),
    DM("DM", "DMA", "Dominica"),
    BS("BS", "BHS", "Bahamas"),
    
    // Oceania
    FJ("FJ", "FJI", "Fiji"),
    PG("PG", "PNG", "Papua New Guinea"),
    SB("SB", "SLB", "Solomon Islands"),
    VU("VU", "VUT", "Vanuatu"),
    NC("NC", "NCL", "New Caledonia"),
    PF("PF", "PYF", "French Polynesia"),
    WS("WS", "WSM", "Samoa"),
    TO("TO", "TON", "Tonga"),
    KI("KI", "KIR", "Kiribati"),
    TV("TV", "TUV", "Tuvalu"),
    NR("NR", "NRU", "Nauru"),
    MH("MH", "MHL", "Marshall Islands"),
    FM("FM", "FSM", "Micronesia"),
    PW("PW", "PLW", "Palau"),
    
    // Other territories
    GL("GL", "GRL", "Greenland"),
    IS("IS", "ISL", "Iceland"),
    FO("FO", "FRO", "Faroe Islands"),
    SJ("SJ", "SJM", "Svalbard and Jan Mayen"),
    AX("AX", "ALA", "Åland Islands"),
    AD("AD", "AND", "Andorra"),
    MC("MC", "MCO", "Monaco"),
    SM("SM", "SMR", "San Marino"),
    VA("VA", "VAT", "Vatican City"),
    LI("LI", "LIE", "Liechtenstein"),
    GI("GI", "GIB", "Gibraltar"),
    IM("IM", "IMN", "Isle of Man"),
    JE("JE", "JEY", "Jersey"),
    GG("GG", "GGY", "Guernsey"),
    FK("FK", "FLK", "Falkland Islands"),
    GS("GS", "SGS", "South Georgia and the South Sandwich Islands"),
    SH("SH", "SHN", "Saint Helena"),
    AC("AC", "ASC", "Ascension Island"),
    TA("TA", "TAA", "Tristan da Cunha"),
    BV("BV", "BVT", "Bouvet Island"),
    HM("HM", "HMD", "Heard Island and McDonald Islands"),
    CC("CC", "CCK", "Cocos Islands"),
    CX("CX", "CXR", "Christmas Island"),
    NF("NF", "NFK", "Norfolk Island"),
    CK("CK", "COK", "Cook Islands"),
    NU("NU", "NIU", "Niue"),
    TK("TK", "TKL", "Tokelau"),
    PN("PN", "PCN", "Pitcairn Islands"),
    WF("WF", "WLF", "Wallis and Futuna"),
    AS("AS", "ASM", "American Samoa"),
    GU("GU", "GUM", "Guam"),
    MP("MP", "MNP", "Northern Mariana Islands"),
    VI("VI", "VIR", "U.S. Virgin Islands"),
    AI("AI", "AIA", "Anguilla"),
    BM("BM", "BMU", "Bermuda"),
    VG("VG", "VGB", "British Virgin Islands"),
    KY("KY", "CYM", "Cayman Islands"),
    TC("TC", "TCA", "Turks and Caicos Islands"),
    MS("MS", "MSR", "Montserrat"),
    AW("AW", "ABW", "Aruba"),
    AN("AN", "ANT", "Netherlands Antilles"),
    CW("CW", "CUW", "Curaçao"),
    SX("SX", "SXM", "Sint Maarten"),
    BQ("BQ", "BES", "Caribbean Netherlands"),
    GP("GP", "GLP", "Guadeloupe"),
    MQ("MQ", "MTQ", "Martinique"),
    BL("BL", "BLM", "Saint Barthélemy"),
    MF("MF", "MAF", "Saint Martin"),
    RE("RE", "REU", "Réunion"),
    YT("YT", "MYT", "Mayotte"),
    IO("IO", "IOT", "British Indian Ocean Territory"),
    TF("TF", "ATF", "French Southern Territories"),
    EH("EH", "ESH", "Western Sahara"),
    XK("XK", "XKX", "Kosovo"),
    TW("TW", "TWN", "Taiwan"),
    HK("HK", "HKG", "Hong Kong"),
    MO("MO", "MAC", "Macau");

    private final String alpha2;
    private final String alpha3;
    private final String name;

    Country(String alpha2, String alpha3, String name) {
        this.alpha2 = alpha2;
        this.alpha3 = alpha3;
        this.name = name;
    }

    public String getAlpha2() {
        return alpha2;
    }

    public String getAlpha3() {
        return alpha3;
    }

    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return alpha2;
    }

    /**
     * Find a Country by its alpha-2 code.
     * 
     * @param code the two-letter country code (e.g., "US", "GB", "FR")
     * @return the Country enum value
     * @throws IllegalArgumentException if the code is not found
     */
    public static Country fromCode(String code) {
        if (code == null) {
            throw new IllegalArgumentException("Country code cannot be null");
        }
        
        for (Country country : values()) {
            if (country.alpha2.equals(code)) {
                return country;
            }
        }
        
        throw new IllegalArgumentException("Unknown country code: " + code);
    }
}
