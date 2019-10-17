/*
 * Copyright 2006 by Heiner Jostkleigrewe
 * Diese Datei steht unter LGPL - siehe beigefügte lpgl.txt
 */
package de.jost_net.OBanToo.Dtaus;

import java.io.DataOutputStream;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * A-Satz - Datei-Vorsatz
 * 
 * @author Heiner Jostkleigrewe
 * 
 */
public class ASatz extends Satz
{

  /**
   * Feld a01, 4 Bytes, numerisch, Satzlängenfeld, Konstant 0128
   */
  private String aSatzlaenge = "0128";

  /**
   * Feld a02, 1 Byte, alpha, Satzart, Konstant A
   */
  private String aSatzart = "A";

  /**
   * Feld a03, 2 Byte, alpha, Kennzeichen GK oder LK, Hinweis auf Gutschriften
   * (G) bzw. Lastschriften (L), K = Kundendatei
   */
  private String aGutschriftLastschrift = null;

  /**
   * Feld a04, 8 Byte, numerisch, Bankleitzahl, Bankleitzahl des Kreditinstituts
   * (Dateiempfänger)
   */
  private long aBlz = 0;

  /**
   * Feld a05, 8 Byte, Dateien, die von Banken geliefert werden, enthalten hier
   * die BLZ
   */
  private long aFeld5;

  /**
   * Feld a06, 27 Byte, alpha, Kundenname, Dateiabsender
   */
  private String aKundenname = null;

  /**
   * Feld a07, 6 Byte, numerisch, Datum, Dateierstellungsdatum (TTMMJJ)
   */
  private String aDateierstellungsdatum = null;

  /**
   * Feld a09, 10 Byte, numerisch, Kontonummer. Empfänger/Absender Kunde, max 10
   * Stellen. Über dieses Konto wird der Gegenwert verrechnet.
   */
  private long aKonto = 0;

  /**
   * Feld a10, 10 Byte, numerisch, Referenznummer des Einreichers, Angabe
   * freigestellt
   */
  private String aReferenz = "          ";

  /**
   * Feld a11b, 8 Byte, alpha, Ausführungsdatum (TTMMJJJJ) Angabe freigestellt.
   * Nicht jünger als Dateierstellungsdatum (Feld A7), jedoch höchstens 15
   * Kalendertage über Erstellungsdatum aus Feld A7. Soweit in diesem Datenfeld
   * ein Ausführungstermin angegeben wird, ist zu beachten, dass der in den
   * Sonderbedingungen genannte Nachweiszeitraum von mindestens 10 Kalendertagen
   * erst ab dem genannten Ausführungstermin zu berechnen ist.
   */
  private Date aAusfuehrungsdatum = null;

  /**
   * Feld a12, 1 Byte, alpha, Währungskennzeichen, konstant '1'
   */
  private String aWaehrungskennzeichen = "1";

  private int toleranz = 0;

  /**
   * Konstruktor mit der Übergabe eines zu parsenden Satzes
   */
  public ASatz(String satz, int toleranz) throws DtausException
  {
    this.toleranz = toleranz;
    satz = codingFromDtaus(satz, toleranz);
    try
    {
      validCharacters(satz);
    }
    catch (DtausException e)
    {
      throw new DtausException(e.getMessage() + " im A-Satz");
    }
    if (!satz.substring(0, 4).equals(aSatzlaenge))
    {
      throw new DtausException(DtausException.A_SATZLAENGENFELD_FEHLERHAFT);
    }
    if (!satz.substring(4, 5).equals(aSatzart))
    {
      throw new DtausException(DtausException.A_SATZART_FEHLERHAFT);
    }
    setGutschriftLastschrift(satz.substring(5, 7));
    setBlz(satz.substring(7, 15));
    setFeld5(satz.substring(15, 23));
    setKundenname(satz.substring(23, 50));
    setDateierstellungsdatum(satz.substring(50, 56));
    setKonto(satz.substring(60, 70));
    setReferenz(satz.substring(70, 80));
    setAusfuehrungsdatum(satz.substring(95, 103));
    setWaehrungskennzeichen(satz.substring(127, 128));
  }

  public ASatz()
  {
    //
  }

  /**
   * Kennzeichen <br>
   * GK = Gutschrift Kunde<br>
   * LK = Lastschrift Kunde<br>
   */
  public void setGutschriftLastschrift(String value) throws DtausException
  {
    if (value.equals("GK") || value.equals("LK") || value.equals("GB")
        || value.equals("LB"))
    {
      aGutschriftLastschrift = value;
    }
    else
    {
      throw new DtausException(
          DtausException.A_GUTSCHRIFT_LASTSCHRIFT_FEHLERHAFT);
    }
  }

  public String getGutschriftLastschrift()
  {
    return aGutschriftLastschrift;
  }

  public void setBlz(String value) throws DtausException
  {
    try
    {
      aBlz = Long.parseLong(value);
    }
    catch (NumberFormatException e)
    {
      throw new DtausException(DtausException.A_BLZ_FEHLERHAFT);
    }
  }

  public void setBlz(long value)
  {
    aBlz = value;
  }

  public long getBlz()
  {
    return aBlz;
  }

  public void setFeld5(String value)
  {
    try
    {
      aFeld5 = Long.parseLong(value);
    }
    catch (NumberFormatException e)
    {
      aFeld5 = 0;
    }
  }

  public void setFeld5(long value)
  {
    aFeld5 = value;
  }

  public long getFeld5()
  {
    return aFeld5;
  }

  public void setKundenname(String value) throws DtausException
  {
    aKundenname = makeValid(value);
    validCharacters(aKundenname);
  }

  public String getKundenname()
  {
    return aKundenname;
  }

  public void setDateierstellungsdatum(String value)
  {
    aDateierstellungsdatum = value;
  }

  public String getDateierstellungsdatum()
  {
    return aDateierstellungsdatum;
  }

  public Date getDateierstellungsdatumAlsDatum()
  {
    Date erstellungsDatum;
    SimpleDateFormat sdf = new SimpleDateFormat("ddMMyy");
    try
    {
      erstellungsDatum = sdf.parse(aDateierstellungsdatum);

    }
    catch (ParseException e)
    {
      erstellungsDatum = null;
    }
    return erstellungsDatum;
  }

  public void setKonto(String value) throws DtausException
  {
    try
    {
      aKonto = Long.parseLong(value);
    }
    catch (NumberFormatException e)
    {
      throw new DtausException(DtausException.A_KONTO_FEHLERHAFT, value);
    }
  }

  public void setKonto(long value)
  {
    aKonto = value;
  }

  public long getKonto()
  {
    return aKonto;
  }

  public void setReferenz(String value)
  {
    aReferenz = value;
  }

  public String getReferenz()
  {
    return aReferenz;
  }

  public void setAusfuehrungsdatum(String value) throws DtausException
  {
    if (value.equals("        "))
    {
      aAusfuehrungsdatum = null;
      return;
    }
    SimpleDateFormat sdf = new SimpleDateFormat("ddMMyyyy");
    try
    {
      aAusfuehrungsdatum = sdf.parse(value);
    }
    catch (ParseException e)
    {
      throw new DtausException(DtausException.A_AUSFUEHRUNGSDATUM_FEHLERHAFT);
    }
  }

  public void setAusfuehrungsdatum(Date value)
  {
    aAusfuehrungsdatum = value;
  }

  public String getAusfuehrungsdatumString()
  {
    SimpleDateFormat sdf = new SimpleDateFormat("ddMMyyyy");
    try
    {
      return sdf.format(aAusfuehrungsdatum);
    }
    catch (NullPointerException e)
    {
      return "";
    }
  }

  public Date getAusfuehrungsdatum()
  {
    return aAusfuehrungsdatum;
  }

  public void setWaehrungskennzeichen(String value) throws DtausException
  {
    if (value.equals("1"))
    {
      aWaehrungskennzeichen = value;
    }
    else
    {
      if ((toleranz
          & DtausDateiParser.FALSCHESWAEHRUNGSKENNZEICHENERLAUBT) == DtausDateiParser.FALSCHESWAEHRUNGSKENNZEICHENERLAUBT)
      {
        System.out
            .println(DtausException.A_WAEHRUNGSKENNZEICHEN_FEHLERHAFT + value);
      }
      else
      {
        throw new DtausException(
            DtausException.A_WAEHRUNGSKENNZEICHEN_FEHLERHAFT, value);
      }
    }

  }

  public void write(DataOutputStream dos) throws IOException
  {
    // Feld 1 - Satzlänge
    dos.writeBytes("0128");
    // Feld 2 - Satzart
    dos.writeBytes("A");
    // Feld 3 - Gutschrift/Lastschrift
    dos.writeBytes(aGutschriftLastschrift);
    // Feld 4 - Bankleitzahl der Bank, bei der die Diskette eingereicht wird
    dos.writeBytes(Tool.formatBLZ(aBlz));
    // Feld 5 - Konstant 0
    if (aFeld5 != 0)
      dos.writeBytes(Long.toString(aFeld5));
    else
      dos.writeBytes("00000000");
    // Feld 6 - Auftraggeber
    dos.writeBytes(make27(aKundenname));
    // Feld 7 - Datum
    if (aDateierstellungsdatum != null)
      dos.writeBytes(aDateierstellungsdatum);
    else
      dos.writeBytes(Tool.space(6));
    // Feld 8 - Konstant 4 Leerzeichen
    dos.writeBytes(Tool.space(4));
    // Feld 9 - Kontonummer des Auftraggebers
    DecimalFormat dfKonto = new DecimalFormat("0000000000");
    dos.writeBytes(dfKonto.format(aKonto));
    // Feld 10 - Referenznummer des Einreichers
    if (aReferenz != null)
      dos.writeBytes(aReferenz);
    else
      dos.writeBytes("0000000000");
    // Feld 11a - Reserve
    dos.writeBytes(Tool.space(15));
    // Feld 11b - Ausführungsdatum
    if (aAusfuehrungsdatum != null)
    {
      SimpleDateFormat sdf11 = new SimpleDateFormat("ddMMyyyy");
      dos.writeBytes(sdf11.format(aAusfuehrungsdatum));
    }
    else
    {
      dos.writeBytes(Tool.space(8));
    }
    // Feld 11c - Reserve
    dos.writeBytes(Tool.space(24));
    // Feld 12 - Währung
    dos.writeBytes(aWaehrungskennzeichen);
  }

  @Override
  public String toString()
  {
    return "Satzlaenge=" + aSatzlaenge + ", Satzart=" + aSatzart
        + ", Gutschrift/Lastschrift=" + aGutschriftLastschrift + ", BLZ=" + aBlz
        + ", Feld5=" + aFeld5 + ", Kundenname=" + aKundenname
        + ", Dateierstellungsdatum=" + aDateierstellungsdatum + ", Konto="
        + aKonto + ", Referenz=" + aReferenz + ", Ausführungsdatum="
        + aAusfuehrungsdatum + ", Währungskennzeichen=" + aWaehrungskennzeichen;
  }
}