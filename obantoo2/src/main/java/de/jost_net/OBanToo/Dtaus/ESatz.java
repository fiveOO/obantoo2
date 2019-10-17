/*
 * Copyright 2006 by Heiner Jostkleigrewe
 * Diese Datei steht unter LGPL - siehe beigefügte lpgl.txt
 */
package de.jost_net.OBanToo.Dtaus;

import java.io.DataOutputStream;
import java.io.IOException;
import java.math.BigInteger;

/**
 * E-Satz - Datei-Nachsatz
 * 
 * @author Heiner Jostkleigrewe
 * 
 */
public class ESatz extends Satz
{

  /**
   * Feld e01, 4 Bytes, numerisch, Satzlängenfeld, Konstant 0128
   */
  private String eSatzlaenge = "0128";

  /**
   * Feld e02, 1 Byte, alpha, Satzart, Konstant E
   */
  private String eSatzart = "E";

  /**
   * Feld e04, 7 Bytes, numerisch, Anzahl der Datensätze C, Abstimm-Daten
   */
  private int eAnzahlC = 0;

  /**
   * Feld e06, 17 Bytes, numerisch, Summe der Kontonummern, Abstimm-Unterlage
   */
  private BigInteger eSummeKontonummern = new BigInteger("0");

  /**
   * Feld e07, 17 Bytes, numerisch, Summe der Bankleitzahlen, Abstimm-Unterlage
   */
  private BigInteger eSummeBankleitzahlen = new BigInteger("0");

  /**
   * Feld e08, 13 Bytes, numerisch, Summe der Euro-Beträge aus den Datensätzen C
   * (Feld 12)
   */
  private BigInteger eSummeBetraege = new BigInteger("0");

  public ESatz()
  {
    //
  }

  /**
   * Konstruktor mit der Übergabe eines zu parsenden Satzes
   */
  public ESatz(String satz, int toleranz) throws DtausException
  {
    satz = codingFromDtaus(satz, toleranz);
    try
    {
      validCharacters(satz);
    }
    catch (DtausException e)
    {
      throw new DtausException(e.getMessage() + " im E-Satz");
    }

    if (!satz.substring(0, 4).equals(eSatzlaenge))
    {
      throw new DtausException(DtausException.E_SATZLAENGENFELD_FEHLERHAFT);
    }
    if (!satz.substring(4, 5).equals(eSatzart))
    {
      throw new DtausException(DtausException.E_SATZART_FEHLERHAFT);
    }
    setAnzahlDatensaetze(satz.substring(10, 17));
    setSummeKontonummern(satz.substring(30, 47));
    setSummeBankleitzahlen(satz.substring(47, 64));
    setSummeBetraege(satz.substring(64, 77));
  }

  public void setAnzahlDatensaetze(String value) throws DtausException
  {
    try
    {
      eAnzahlC = Integer.parseInt(value);
    }
    catch (NumberFormatException e)
    {
      throw new DtausException(DtausException.E_ANZAHL_CSAETZE_FEHLERHAFT,
          value);
    }
  }

  public int getAnzahlDatensaetze()
  {
    return eAnzahlC;
  }

  public void setSummeKontonummern(String value)
  {
    eSummeKontonummern = new BigInteger(value);
  }

  public BigInteger getSummeKontonummern()
  {
    return eSummeKontonummern;
  }

  public void setSummeBankleitzahlen(String value)
  {
    eSummeBankleitzahlen = new BigInteger(value);
  }

  public BigInteger getSummeBankleitzahlen()
  {
    return this.eSummeBankleitzahlen;
  }

  public void setSummeBetraege(String value) throws DtausException
  {
    try
    {
      eSummeBetraege = new BigInteger(value);
    }
    catch (NumberFormatException e)
    {
      throw new DtausException(DtausException.E_SUMME_BETRAEGE_FEHLERHAFT,
          value);
    }
  }

  public BigInteger getSummeBetraege()
  {
    return this.eSummeBetraege;
  }

  public void add(CSatz csatz)
  {
    this.eAnzahlC++;
    this.eSummeBankleitzahlen = this.eSummeBankleitzahlen
        .add(new BigInteger(csatz.getBlzEndbeguenstigt() + ""));
    this.eSummeKontonummern = this.eSummeKontonummern
        .add(new BigInteger(csatz.getKontonummer() + ""));
    this.eSummeBetraege = this.eSummeBetraege
        .add(new BigInteger(csatz.getBetragInCent() + ""));
  }

  public void write(DataOutputStream dos) throws IOException
  {
    // Feld 1 - Satzlängenfeld
    dos.writeBytes("0128");
    // Feld 2 - Satzart
    dos.writeBytes("E");
    // Feld 3 - Reserve
    dos.writeBytes(Tool.space(5));
    // Feld 4 - Anzahl Datensätze C
    dos.writeBytes(Tool.formatKontollAnzahl(this.eAnzahlC));
    // Feld 5 - Summe der DM-Beträge
    dos.writeBytes(Tool.formatKontrollSumme(new BigInteger("0")));
    // Feld 6 - Summe der Konto-Nummern
    dos.writeBytes(Tool.formatKontroll17(this.eSummeKontonummern));
    // Feld 7 - Summe der Bankleitzahlen
    dos.writeBytes(Tool.formatKontroll17(this.eSummeBankleitzahlen));
    // Feld 8 - Summe der Beträge in Euro
    dos.writeBytes(Tool.formatKontrollSumme(this.eSummeBetraege));
    // Feld 9 - Reserve
    dos.writeBytes(Tool.space(51));

  }

  @Override
  public String toString()
  {
    return "Satzlaenge=" + eSatzlaenge + ", Satzart=" + eSatzart
        + ", Anzahl C-Sätze=" + eAnzahlC + ", Summe Kontonummern="
        + eSummeKontonummern.toString() + ", Summe Bankleitzahlen="
        + eSummeBankleitzahlen.toString() + ", Summe Beträge=" + eSummeBetraege;
  }
}
