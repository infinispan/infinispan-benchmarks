package org.infinispan;

import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

import org.infinispan.api.annotations.indexing.Basic;
import org.infinispan.api.annotations.indexing.Indexed;
import org.infinispan.protostream.annotations.ProtoFactory;
import org.infinispan.protostream.annotations.ProtoField;

@Indexed
public class CacheValue {

      // --- 1. Fields with explicit @ProtoField and default values ---

      @ProtoField(number = 1, defaultValue = "0")
      public long dataDiCaricamento;

      @Basic(sortable = true, aggregable = true)
      @ProtoField(number = 2, defaultValue = "0")
      public long idProgressivo;

      @Basic(sortable = true, aggregable = true)
      @ProtoField(number = 3)
      public String nomeFile;

      @Basic(sortable = true, aggregable = true)
      @ProtoField(number = 4)
      public String reportingCounterParty;

      @Basic(sortable = true, aggregable = true)
      @ProtoField(number = 5)
      public String otherCounterParty;

      @Basic(sortable = true, aggregable = true)
      @ProtoField(number = 6)
      public String counterPartySide;

      @Basic(sortable = true, aggregable = true)
      @ProtoField(number = 7)
      public String triPartyAgent;

      @Basic(sortable = true, aggregable = true)
      @ProtoField(number = 8)
      public String agentLender;

      @Basic(sortable = true, aggregable = true, name = "Count_of_UTI")
      @ProtoField(number = 9, defaultValue = "0")
      public long Count_of_UTI;

      @Basic(sortable = true, aggregable = true, name = "Type_of_SFT")
      @ProtoField(number = 10)
      public String Type_of_SFT;

      @Basic(sortable = true, aggregable = true)
      @ProtoField(number = 11)
      public String cleared;

      @Basic(sortable = true, aggregable = true)
      @ProtoField(number = 12)
      public String tradingVenue;

      @ProtoField(number = 13)
      public String masterAgreementType;

      @ProtoField(number = 14, defaultValue = "0")
      public long maturityDate;

      @ProtoField(number = 15)
      public String generalCollateralIndicator;

      @ProtoField(number = 16)
      public String openTerm;

      @ProtoField(number = 17)
      public String ratesFixed;

      @ProtoField(number = 18)
      public String ratesFloating;

      @ProtoField(number = 19)
      public String ratesBsbCalculated;

      @ProtoField(number = 20)
      public String lendingFee;

      @ProtoField(number = 21, defaultValue = "0.0")
      public double principalAmountOnValueDate;

      @ProtoField(number = 22)
      public String principalAmountCurrency;

      @ProtoField(number = 23)
      public String priceCurrency1;

      @ProtoField(number = 24)
      public String securityQuality;

      @ProtoField(number = 25)
      public String securityType;

      @ProtoField(number = 26)
      public String marketValue;

      @ProtoField(number = 27)
      public String outstandingMarginLoan;

      @ProtoField(number = 28)
      public String baseCurrencyOfOutstandingMarginLoan;

      @ProtoField(number = 29)
      public String shortMarketValue;

      @ProtoField(number = 30)
      public String collateralisationOfNetExposure;

      @Basic(sortable = true, aggregable = true)
      @ProtoField(number = 31)
      public String typeOfCollateralComponent;

      @ProtoField(number = 32, defaultValue = "0.0")
      public double cashCollateralAmount;

      @ProtoField(number = 33)
      public String cashCollateralCurrency;

      @ProtoField(number = 34, defaultValue = "0.0")
      public double collateralQuantityOrNominalAmount;

      @ProtoField(number = 35)
      public String priceCurrency2;

      @ProtoField(number = 36)
      public String collateraMarketValue;

      @ProtoField(number = 37)
      public String haircutOrMargin;

      @Basic(sortable = true, aggregable = true)
      @ProtoField(number = 38)
      public String collateralQuality;

      @ProtoField(number = 39, defaultValue = "0")
      public long maturityDateOfTheSecurity;

      @ProtoField(number = 40)
      public String jurisdictionOfTheIssuer;

      @Basic(sortable = true, aggregable = true)
      @ProtoField(number = 41)
      public String collateralType;

      @ProtoField(number = 42)
      public String trToWhichTheOtherCounterpartyReported;

      @Basic(sortable = true, aggregable = true)
      @ProtoField(number = 43)
      public String reconciliationStatus;

      @ProtoField(number = 44)
      public String initialMarginPosted;

      @ProtoField(number = 45)
      public String variationMarginPosted;

      @ProtoField(number = 46)
      public String excessCollateralPosted;

      @ProtoField(number = 47)
      public String valueOfReusedCollateral;

      @ProtoField(number = 48)
      public String estimatedReuseOfCollateral;

      @ProtoField(number = 49)
      public String reinvestmentRate;

      @ProtoField(number = 50)
      public String typeOfReinvestedCashInvestment;

      @ProtoField(number = 51)
      public String reinvestedCashAmount;

      @ProtoField(number = 52)
      public String businessMessageIdentifier;

      @ProtoField(number = 53)
      public String messageDefinitionIdentifier;

      @ProtoField(number = 54)
      public String businessService;

      @ProtoField(number = 55, defaultValue = "0")
      public long creationDate;

      @ProtoField(number = 56)
      public String technicalRecordIdentification;

      @ProtoField(number = 57, defaultValue = "0")
      public long dataDiRicezione;

      @ProtoField(number = 58)
      public String tradeRepository;

      @ProtoField(number = 59)
      public String flusso;

      @Basic(sortable = true, aggregable = true, name = "Received_Report_Date")
      @ProtoField(number = 60, defaultValue = "0")
      public long Received_Report_Date;

      @Basic(sortable = true, aggregable = true)
      @ProtoField(number = 61)
      public String actionType;

      @ProtoFactory
      public CacheValue(long dataDiCaricamento, long idProgressivo, String nomeFile, String reportingCounterParty,
                        String otherCounterParty, String counterPartySide, String triPartyAgent, String agentLender,
                        long Count_of_UTI, String Type_of_SFT, String cleared, String tradingVenue,
                        String masterAgreementType, long maturityDate, String generalCollateralIndicator,
                        String openTerm, String ratesFixed, String ratesFloating, String ratesBsbCalculated,
                        String lendingFee, double principalAmountOnValueDate, String principalAmountCurrency,
                        String priceCurrency1, String securityQuality, String securityType, String marketValue,
                        String outstandingMarginLoan, String baseCurrencyOfOutstandingMarginLoan, String shortMarketValue,
                        String collateralisationOfNetExposure, String typeOfCollateralComponent, double cashCollateralAmount,
                        String cashCollateralCurrency, double collateralQuantityOrNominalAmount, String priceCurrency2,
                        String collateraMarketValue, String haircutOrMargin, String collateralQuality,
                        long maturityDateOfTheSecurity, String jurisdictionOfTheIssuer, String collateralType,
                        String trToWhichTheOtherCounterpartyReported, String reconciliationStatus, String initialMarginPosted,
                        String variationMarginPosted, String excessCollateralPosted, String valueOfReusedCollateral,
                        String estimatedReuseOfCollateral, String reinvestmentRate, String typeOfReinvestedCashInvestment,
                        String reinvestedCashAmount, String businessMessageIdentifier, String messageDefinitionIdentifier,
                        String businessService, long creationDate, String technicalRecordIdentification, long dataDiRicezione,
                        String tradeRepository, String flusso, long Received_Report_Date, String actionType) {
            this.dataDiCaricamento = dataDiCaricamento;
            this.idProgressivo = idProgressivo;
            this.nomeFile = nomeFile;
            this.reportingCounterParty = reportingCounterParty;
            this.otherCounterParty = otherCounterParty;
            this.counterPartySide = counterPartySide;
            this.triPartyAgent = triPartyAgent;
            this.agentLender = agentLender;
            this.Count_of_UTI = Count_of_UTI;
            this.Type_of_SFT = Type_of_SFT;
            this.cleared = cleared;
            this.tradingVenue = tradingVenue;
            this.masterAgreementType = masterAgreementType;
            this.maturityDate = maturityDate;
            this.generalCollateralIndicator = generalCollateralIndicator;
            this.openTerm = openTerm;
            this.ratesFixed = ratesFixed;
            this.ratesFloating = ratesFloating;
            this.ratesBsbCalculated = ratesBsbCalculated;
            this.lendingFee = lendingFee;
            this.principalAmountOnValueDate = principalAmountOnValueDate;
            this.principalAmountCurrency = principalAmountCurrency;
            this.priceCurrency1 = priceCurrency1;
            this.securityQuality = securityQuality;
            this.securityType = securityType;
            this.marketValue = marketValue;
            this.outstandingMarginLoan = outstandingMarginLoan;
            this.baseCurrencyOfOutstandingMarginLoan = baseCurrencyOfOutstandingMarginLoan;
            this.shortMarketValue = shortMarketValue;
            this.collateralisationOfNetExposure = collateralisationOfNetExposure;
            this.typeOfCollateralComponent = typeOfCollateralComponent;
            this.cashCollateralAmount = cashCollateralAmount;
            this.cashCollateralCurrency = cashCollateralCurrency;
            this.collateralQuantityOrNominalAmount = collateralQuantityOrNominalAmount;
            this.priceCurrency2 = priceCurrency2;
            this.collateraMarketValue = collateraMarketValue;
            this.haircutOrMargin = haircutOrMargin;
            this.collateralQuality = collateralQuality;
            this.maturityDateOfTheSecurity = maturityDateOfTheSecurity;
            this.jurisdictionOfTheIssuer = jurisdictionOfTheIssuer;
            this.collateralType = collateralType;
            this.trToWhichTheOtherCounterpartyReported = trToWhichTheOtherCounterpartyReported;
            this.reconciliationStatus = reconciliationStatus;
            this.initialMarginPosted = initialMarginPosted;
            this.variationMarginPosted = variationMarginPosted;
            this.excessCollateralPosted = excessCollateralPosted;
            this.valueOfReusedCollateral = valueOfReusedCollateral;
            this.estimatedReuseOfCollateral = estimatedReuseOfCollateral;
            this.reinvestmentRate = reinvestmentRate;
            this.typeOfReinvestedCashInvestment = typeOfReinvestedCashInvestment;
            this.reinvestedCashAmount = reinvestedCashAmount;
            this.businessMessageIdentifier = businessMessageIdentifier;
            this.messageDefinitionIdentifier = messageDefinitionIdentifier;
            this.businessService = businessService;
            this.creationDate = creationDate;
            this.technicalRecordIdentification = technicalRecordIdentification;
            this.dataDiRicezione = dataDiRicezione;
            this.tradeRepository = tradeRepository;
            this.flusso = flusso;
            this.Received_Report_Date = Received_Report_Date;
            this.actionType = actionType;
      }

      public static CacheValue create(long receivedDate) {
            ThreadLocalRandom random = ThreadLocalRandom.current();

            String[] sftTypes = {"MGLD", "REPO", "SLEB", "BSBK"};
            String[] sides = {"TAKE", "GIVE", "COLL", "DAIR"};
            String[] collateralTypes = {"OEQU", "GOVT", "BOND", "CASH"};
            String currentSftType = sftTypes[random.nextInt(sftTypes.length)];

            String rptCpty = "LEI" + UUID.randomUUID().toString().replace("-", "").substring(0, 17).toUpperCase();
            String otherCpty = "LEI" + UUID.randomUUID().toString().replace("-", "").substring(0, 17).toUpperCase();

            return new CacheValue(
                  System.currentTimeMillis(),
                  Math.abs(random.nextLong()),
                  "TRRGS_" + currentSftType + "_R00" + random.nextInt(1000) + ".xml",
                  rptCpty,
                  otherCpty,
                  sides[random.nextInt(sides.length)],
                  null, null, 1,
                  currentSftType,
                  null, null, null,
                  0L, null, null, null, null, null, null,
                  0.0, null, null, null, null, null,
                  String.format("%.2f", random.nextDouble() * 1_000_000),
                  null, null, null, null,
                  0.0, null, 0.0, null, null, null, null, 0L, null,
                  collateralTypes[random.nextInt(collateralTypes.length)],
                  "TRRGS",
                  null, null, null, null, null, null, null, null, null,
                  UUID.randomUUID().toString().substring(0, 15),
                  "auth.105.001.01",
                  "SFTR_PROD",
                  System.currentTimeMillis() - 3600000,
                  UUID.randomUUID().toString(),
                  System.currentTimeMillis(),
                  "TRRGS",
                  "SFTPOS",
                  receivedDate,
                  "Overview"
            );
      }
}
