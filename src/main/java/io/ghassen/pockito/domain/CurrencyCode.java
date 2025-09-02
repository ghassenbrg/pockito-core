package io.ghassen.pockito.domain;

/**
 * Currency code enum following ISO 4217 standard.
 * 
 * This enum provides the three-letter currency codes (alpha-3) as defined by ISO 4217,
 * along with their numeric codes and full names for reference.
 * 
 * @see <a href="https://en.wikipedia.org/wiki/ISO_4217">ISO 4217</a>
 */
public enum CurrencyCode {
    // Major world currencies
    USD("USD", 840, "US Dollar"),
    EUR("EUR", 978, "Euro"),
    GBP("GBP", 826, "British Pound Sterling"),
    JPY("JPY", 392, "Japanese Yen"),
    CHF("CHF", 756, "Swiss Franc"),
    CAD("CAD", 124, "Canadian Dollar"),
    AUD("AUD", 036, "Australian Dollar"),
    CNY("CNY", 156, "Chinese Yuan"),
    HKD("HKD", 344, "Hong Kong Dollar"),
    SEK("SEK", 752, "Swedish Krona"),
    NOK("NOK", 578, "Norwegian Krone"),
    DKK("DKK", 208, "Danish Krone"),
    PLN("PLN", 985, "Polish Złoty"),
    CZK("CZK", 203, "Czech Koruna"),
    HUF("HUF", 348, "Hungarian Forint"),
    RON("RON", 946, "Romanian Leu"),
    BGN("BGN", 975, "Bulgarian Lev"),
    HRK("HRK", 191, "Croatian Kuna"),
    RUB("RUB", 643, "Russian Ruble"),
    TRY("TRY", 949, "Turkish Lira"),
    BRL("BRL", 986, "Brazilian Real"),
    MXN("MXN", 484, "Mexican Peso"),
    ARS("ARS", 032, "Argentine Peso"),
    CLP("CLP", 152, "Chilean Peso"),
    COP("COP", 170, "Colombian Peso"),
    PEN("PEN", 604, "Peruvian Sol"),
    UYU("UYU", 858, "Uruguayan Peso"),
    VES("VES", 928, "Venezuelan Bolívar"),
    KRW("KRW", 410, "South Korean Won"),
    SGD("SGD", 702, "Singapore Dollar"),
    TWD("TWD", 901, "New Taiwan Dollar"),
    THB("THB", 764, "Thai Baht"),
    MYR("MYR", 458, "Malaysian Ringgit"),
    IDR("IDR", 360, "Indonesian Rupiah"),
    PHP("PHP", 608, "Philippine Peso"),
    VND("VND", 704, "Vietnamese Dong"),
    INR("INR", 356, "Indian Rupee"),
    PKR("PKR", 586, "Pakistani Rupee"),
    BDT("BDT", 050, "Bangladeshi Taka"),
    LKR("LKR", 144, "Sri Lankan Rupee"),
    NPR("NPR", 524, "Nepalese Rupee"),
    MMK("MMK", 104, "Myanmar Kyat"),
    KHR("KHR", 116, "Cambodian Riel"),
    LAK("LAK", 418, "Lao Kip"),
    MNT("MNT", 496, "Mongolian Tögrög"),
    KZT("KZT", 398, "Kazakhstani Tenge"),
    UZS("UZS", 860, "Uzbekistani Som"),
    TJS("TJS", 972, "Tajikistani Somoni"),
    TMT("TMT", 934, "Turkmenistan Manat"),
    AZN("AZN", 944, "Azerbaijani Manat"),
    GEL("GEL", 981, "Georgian Lari"),
    AMD("AMD", 051, "Armenian Dram"),
    KGS("KGS", 417, "Kyrgyzstani Som"),
    UAH("UAH", 980, "Ukrainian Hryvnia"),
    BYN("BYN", 933, "Belarusian Ruble"),
    MDL("MDL", 498, "Moldovan Leu"),
    RSD("RSD", 941, "Serbian Dinar"),
    BAM("BAM", 977, "Bosnia and Herzegovina Convertible Mark"),
    MKD("MKD", 807, "Macedonian Denar"),
    ALL("ALL", 8, "Albanian Lek"),
    XCD("XCD", 951, "East Caribbean Dollar"),
    BBD("BBD", 52, "Barbadian Dollar"),
    JMD("JMD", 388, "Jamaican Dollar"),
    TTD("TTD", 780, "Trinidad and Tobago Dollar"),
    BZD("BZD", 84, "Belize Dollar"),
    GYD("GYD", 328, "Guyanese Dollar"),
    SRD("SRD", 968, "Surinamese Dollar"),
    FJD("FJD", 242, "Fijian Dollar"),
    WST("WST", 882, "Samoan Tālā"),
    TOP("TOP", 776, "Tongan Paʻanga"),
    VUV("VUV", 548, "Vanuatu Vatu"),
    PGK("PGK", 598, "Papua New Guinean Kina"),
    SBD("SBD", 90, "Solomon Islands Dollar"),
    KID("KID", 174, "Kiribati Dollar"),
    TVD("TVD", 901, "Tuvaluan Dollar"),
    NIO("NIO", 558, "Nicaraguan Córdoba"),
    GTQ("GTQ", 320, "Guatemalan Quetzal"),
    HNL("HNL", 340, "Honduran Lempira"),
    SVC("SVC", 222, "Salvadoran Colón"),
    PAB("PAB", 590, "Panamanian Balboa"),
    CRC("CRC", 188, "Costa Rican Colón"),
    BOB("BOB", 68, "Bolivian Boliviano"),
    EGP("EGP", 818, "Egyptian Pound"),
    MAD("MAD", 504, "Moroccan Dirham"),
    TND("TND", 788, "Tunisian Dinar"),
    DZD("DZD", 12, "Algerian Dinar"),
    LYD("LYD", 434, "Libyan Dinar"),
    SDG("SDG", 938, "Sudanese Pound"),
    NGN("NGN", 566, "Nigerian Naira"),
    KES("KES", 404, "Kenyan Shilling"),
    GHS("GHS", 936, "Ghanaian Cedi"),
    XOF("XOF", 952, "West African CFA Franc"),
    XAF("XAF", 950, "Central African CFA Franc"),
    XPF("XPF", 953, "CFP Franc"),
    ZAR("ZAR", 710, "South African Rand"),
    BWP("BWP", 72, "Botswana Pula"),
    NAM("NAM", 516, "Namibian Dollar"),
    LSL("LSL", 426, "Lesotho Loti"),
    SZL("SZL", 748, "Eswatini Lilangeni"),
    ZMW("ZMW", 967, "Zambian Kwacha"),
    ZWL("ZWL", 932, "Zimbabwean Dollar"),
    MWK("MWK", 454, "Malawian Kwacha"),
    TZS("TZS", 834, "Tanzanian Shilling"),
    UGX("UGX", 800, "Ugandan Shilling"),
    RWF("RWF", 646, "Rwandan Franc"),
    BIF("BIF", 108, "Burundian Franc"),
    KMF("KMF", 174, "Comorian Franc"),
    DJF("DJF", 262, "Djiboutian Franc"),
    SOS("SOS", 706, "Somali Shilling"),
    ERN("ERN", 232, "Eritrean Nakfa"),
    STN("STN", 930, "São Tomé and Príncipe Dobra"),
    CVE("CVE", 132, "Cape Verdean Escudo"),
    GMD("GMD", 270, "Gambian Dalasi"),
    GNF("GNF", 324, "Guinean Franc"),
    SLL("SLL", 694, "Sierra Leonean Leone"),
    LRD("LRD", 430, "Liberian Dollar"),
    GIP("GIP", 292, "Gibraltar Pound"),
    FKP("FKP", 238, "Falkland Islands Pound"),
    SHP("SHP", 654, "Saint Helena Pound"),
    IMP("IMP", 833, "Manx Pound"),
    JEP("JEP", 832, "Jersey Pound"),
    GGP("GGP", 831, "Guernsey Pound"),
    ANG("ANG", 532, "Netherlands Antillean Guilder"),
    AWG("AWG", 533, "Aruban Florin"),
    BMD("BMD", 60, "Bermudian Dollar"),
    KYD("KYD", 136, "Cayman Islands Dollar"),
    BND("BND", 96, "Brunei Dollar"),
    MOP("MOP", 446, "Macanese Pataca"),
    BTN("BTN", 64, "Bhutanese Ngultrum"),
    MVR("MVR", 462, "Maldivian Rufiyaa"),
    SCR("SCR", 690, "Seychellois Rupee"),
    MUR("MUR", 480, "Mauritian Rupee"),
    CDF("CDF", 976, "Congolese Franc"),
    ETB("ETB", 230, "Ethiopian Birr"),
    XDR("XDR", 960, "Special Drawing Rights"),
    XAU("XAU", 959, "Gold"),
    XAG("XAG", 961, "Silver"),
    XPT("XPT", 962, "Platinum"),
    XPD("XPD", 963, "Palladium");

    private final String code;
    private final int numericCode;
    private final String name;

    CurrencyCode(String code, int numericCode, String name) {
        this.code = code;
        this.numericCode = numericCode;
        this.name = name;
    }

    public String getCode() {
        return code;
    }

    public int getNumericCode() {
        return numericCode;
    }

    public String getName() {
        return name;
    }

    /**
     * Find currency by three-letter code
     */
    public static CurrencyCode fromCode(String code) {
        if (code == null) {
            return null;
        }
        for (CurrencyCode currency : values()) {
            if (currency.code.equalsIgnoreCase(code)) {
                return currency;
            }
        }
        throw new IllegalArgumentException("Unknown currency code: " + code);
    }

    /**
     * Find currency by numeric code
     */
    public static CurrencyCode fromNumericCode(int numericCode) {
        for (CurrencyCode currency : values()) {
            if (currency.numericCode == numericCode) {
                return currency;
            }
        }
        throw new IllegalArgumentException("Unknown numeric currency code: " + numericCode);
    }

    @Override
    public String toString() {
        return code;
    }
}
