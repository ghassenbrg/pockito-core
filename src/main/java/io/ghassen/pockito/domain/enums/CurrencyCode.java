package io.ghassen.pockito.domain.enums;

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
    JPY("JPY", 392, "Japanese Yen"),
    CNY("CNY", 156, "Chinese Yuan"),
    HKD("HKD", 344, "Hong Kong Dollar"),
    PLN("PLN", 985, "Polish Złoty"),
    CZK("CZK", 203, "Czech Koruna"),
    HUF("HUF", 348, "Hungarian Forint"),
    RUB("RUB", 643, "Russian Ruble"),
    TRY("TRY", 949, "Turkish Lira"),
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
    BDT("BDT", 50, "Bangladeshi Taka"),
    LKR("LKR", 144, "Sri Lankan Rupee"),
    NPR("NPR", 524, "Nepalese Rupee"),
    MMK("MMK", 104, "Myanmar Kyat"),
    KHR("KHR", 116, "Cambodian Riel"),
    LAK("LAK", 418, "Lao Kip"),
    BND("BND", 96, "Brunei Dollar"),
    MNT("MNT", 496, "Mongolian Tugrik"),
    KZT("KZT", 398, "Kazakhstani Tenge"),
    UZS("UZS", 860, "Uzbekistani Som"),
    KGS("KGS", 417, "Kyrgyzstani Som"),
    TJS("TJS", 972, "Tajikistani Somoni"),
    TMT("TMT", 934, "Turkmenistani Manat"),
    AFN("AFN", 971, "Afghan Afghani"),
    IRR("IRR", 364, "Iranian Rial"),
    IQD("IQD", 368, "Iraqi Dinar"),
    SYP("SYP", 760, "Syrian Pound"),
    LBP("LBP", 422, "Lebanese Pound"),
    JOD("JOD", 400, "Jordanian Dinar"),
    ILS("ILS", 376, "Israeli New Shekel"),
    EGP("EGP", 818, "Egyptian Pound"),
    LYD("LYD", 434, "Libyan Dinar"),
    TND("TND", 788, "Tunisian Dinar"),
    DZD("DZD", 12, "Algerian Dinar"),
    MAD("MAD", 504, "Moroccan Dirham"),
    SDG("SDG", 938, "Sudanese Pound"),
    SSP("SSP", 728, "South Sudanese Pound"),
    ETB("ETB", 230, "Ethiopian Birr"),
    ERN("ERN", 232, "Eritrean Nakfa"),
    DJF("DJF", 262, "Djiboutian Franc"),
    SOS("SOS", 706, "Somali Shilling"),
    KES("KES", 404, "Kenyan Shilling"),
    UGX("UGX", 800, "Ugandan Shilling"),
    TZS("TZS", 834, "Tanzanian Shilling"),
    RWF("RWF", 646, "Rwandan Franc"),
    BIF("BIF", 108, "Burundian Franc"),
    CDF("CDF", 976, "Congolese Franc"),
    XPF("XPF", 953, "CFP Franc"),
    NGN("NGN", 566, "Nigerian Naira"),
    XOF("XOF", 952, "West African CFA Franc"),
    ZAR("ZAR", 710, "South African Rand"),
    NAD("NAD", 516, "Namibian Dollar"),
    BWP("BWP", 72, "Botswana Pula"),
    ZWL("ZWL", 932, "Zimbabwean Dollar"),
    ZMW("ZMW", 967, "Zambian Kwacha"),
    MWK("MWK", 454, "Malawian Kwacha"),
    MZN("MZN", 943, "Mozambican Metical"),
    MGA("MGA", 969, "Malagasy Ariary"),
    MUR("MUR", 480, "Mauritian Rupee"),
    SCR("SCR", 690, "Seychellois Rupee"),
    KMF("KMF", 174, "Comorian Franc"),
    STN("STN", 930, "São Tomé and Príncipe Dobra"),
    CVE("CVE", 132, "Cape Verdean Escudo"),
    AOA("AOA", 973, "Angolan Kwanza"),
    XAF("XAF", 950, "Central African CFA Franc"),
    SLL("SLL", 694, "Sierra Leonean Leone"),
    LRD("LRD", 430, "Liberian Dollar"),
    MRO("MRO", 478, "Mauritanian Ouguiya"),
    LSL("LSL", 426, "Lesotho Loti"),
    SZL("SZL", 748, "Swazi Lilangeni"),
    GMD("GMD", 270, "Gambian Dalasi"),
    GNF("GNF", 324, "Guinean Franc"),
    GWP("GWP", 624, "Guinea-Bissau Peso"),
    MRT("MRT", 929, "Mauritanian Ouguiya"),
    AMD("AMD", 51, "Armenian Dram"),
    AZN("AZN", 944, "Azerbaijani Manat"),
    GEL("GEL", 981, "Georgian Lari"),
    UAH("UAH", 980, "Ukrainian Hryvnia"),
    BYN("BYN", 933, "Belarusian Ruble"),
    MDL("MDL", 498, "Moldovan Leu"),
    RON("RON", 946, "Romanian Leu"),
    BGN("BGN", 975, "Bulgarian Lev"),
    HRK("HRK", 191, "Croatian Kuna"),
    RSD("RSD", 941, "Serbian Dinar"),
    MKD("MKD", 807, "Macedonian Denar"),
    ALL("ALL", 8, "Albanian Lek"),
    BAM("BAM", 977, "Bosnia and Herzegovina Convertible Mark"),
    ISK("ISK", 352, "Icelandic Króna"),
    NOK("NOK", 578, "Norwegian Krone"),
    DKK("DKK", 208, "Danish Krone"),
    SEK("SEK", 752, "Swedish Krona"),
    CHF("CHF", 756, "Swiss Franc"),
    GBP("GBP", 826, "British Pound Sterling"),
    EUR("EUR", 978, "Euro"),
    USD("USD", 840, "US Dollar"),
    CAD("CAD", 124, "Canadian Dollar"),
    MXN("MXN", 484, "Mexican Peso"),
    GTQ("GTQ", 320, "Guatemalan Quetzal"),
    BZD("BZD", 84, "Belize Dollar"),
    SVC("SVC", 222, "Salvadoran Colón"),
    HNL("HNL", 340, "Honduran Lempira"),
    NIO("NIO", 558, "Nicaraguan Córdoba"),
    CRC("CRC", 188, "Costa Rican Colón"),
    PAB("PAB", 590, "Panamanian Balboa"),
    CUP("CUP", 192, "Cuban Peso"),
    JMD("JMD", 388, "Jamaican Dollar"),
    HTG("HTG", 332, "Haitian Gourde"),
    DOP("DOP", 214, "Dominican Peso"),
    TTD("TTD", 780, "Trinidad and Tobago Dollar"),
    BBD("BBD", 52, "Barbadian Dollar"),
    XCD("XCD", 951, "East Caribbean Dollar"),
    AWG("AWG", 533, "Aruban Florin"),
    ANG("ANG", 532, "Netherlands Antillean Guilder"),
    SRD("SRD", 968, "Surinamese Dollar"),
    GYD("GYD", 328, "Guyanese Dollar"),
    VEF("VEF", 937, "Venezuelan Bolívar"),
    COP("COP", 170, "Colombian Peso"),
    PEN("PEN", 604, "Peruvian Sol"),
    CLP("CLP", 152, "Chilean Peso"),
    ARS("ARS", 32, "Argentine Peso"),
    UYU("UYU", 858, "Uruguayan Peso"),
    PYG("PYG", 600, "Paraguayan Guarani"),
    BOB("BOB", 68, "Bolivian Boliviano"),
    VES("VES", 928, "Venezuelan Bolívar"),
    BRL("BRL", 986, "Brazilian Real"),
    AUD("AUD", 36, "Australian Dollar"),
    NZD("NZD", 554, "New Zealand Dollar"),
    FJD("FJD", 242, "Fijian Dollar"),
    PGK("PGK", 598, "Papua New Guinean Kina"),
    SBD("SBD", 90, "Solomon Islands Dollar"),
    VUV("VUV", 548, "Vanuatu Vatu"),
    WST("WST", 882, "Samoan Tala"),
    TOP("TOP", 776, "Tongan Paʻanga"),
    KID("KID", 296, "Kiribati Dollar"),
    TVD("TVD", 901, "Tuvaluan Dollar"),
    NUD("NUD", 901, "Nauru Dollar"),
    MHD("MHD", 901, "Marshall Islands Dollar"),
    FMD("FMD", 901, "Micronesian Dollar"),
    PWD("PWD", 901, "Palauan Dollar");

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

    @Override
    public String toString() {
        return code;
    }

    /**
     * Find a CurrencyCode by its three-letter code.
     * 
     * @param code the three-letter currency code (e.g., "USD", "EUR")
     * @return the CurrencyCode enum value
     * @throws IllegalArgumentException if the code is not found
     */
    public static CurrencyCode fromCode(String code) {
        if (code == null) {
            throw new IllegalArgumentException("Currency code cannot be null");
        }
        
        for (CurrencyCode currency : values()) {
            if (currency.code.equals(code)) {
                return currency;
            }
        }
        
        throw new IllegalArgumentException("Unknown currency code: " + code);
    }
}
