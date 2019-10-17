/*
 * Copyright 2006 by Heiner Jostkleigrewe
 * Diese Datei steht unter LGPL - siehe beigefügte lpgl.txt
 */
package de.jost_net.OBanToo.QIF;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Vector;

/**
 * Parser für QIF-Dateien
 * <p>
 * Mit dem QIF-Parser können QIF-Dateien vom Typ "Bank" geparst werden. Die
 * Daten werden in Objekten vom Typ QIFBuchung abgelegt, die wiederum in einem
 * Vector gespeichert werden.
 * <p>
 * Sollte die zu parsende QIF-Datei fehlerhaft sein, werden entsprechende
 * QIFExceptions geworfen.
 * <p>
 * Mit dem Aufruf der next()-Methode werden die einzelen Buchungen
 * zurückgeliefert. Sobald alle Buchungen zurückgegeben wurden, wird null
 * zurückgeliefert.
 * <p>
 * Beispiel Auflistung aller Buchungen:<br>
 * <code>
 * QIFDateiParser p = new QIFDateiParser("/home/heiner/test.QIF");<br>
 * QIFBuchung b = p.next();<br>
 * while (b != null)<br>
 * {<br>
 *   System.out.println(b);<br>
 *   b = p.next();<br>
 * }<br>
 * </code>
 * 
 * @author Heiner Jostkleigrewe
 */

public class QIFDateiParser
{

  private Vector<QIFBuchung> buchungen;

  /**
   * Buchungsindex
   */
  private int bi = -1;

  public QIFDateiParser(String filename) throws IOException, QIFException
  {
    this(new BufferedInputStream(new FileInputStream(filename)));
  }

  public QIFDateiParser(InputStream is) throws IOException, QIFException
  {
    buchungen = new Vector<>();
    BufferedReader br = new BufferedReader(new InputStreamReader(is));
    String line;
    line = br.readLine();
    if (!line.startsWith("!Type:Bank"))
    {
      throw new QIFException(QIFException.UNGUELGITGER_HEADER, line);
    }
    QIFBuchung buchung = new QIFBuchung();
    while ((line = br.readLine()) != null)
    {
      if (!line.startsWith("^"))
      {
        buchung.add(line);
      }
      else
      {
        buchungen.addElement(buchung);
        buchung = new QIFBuchung();
      }
    }
  }

  public int getAnzahlBuchungen()
  {
    return buchungen.size();
  }

  public QIFBuchung next()
  {
    bi++;
    if (bi >= buchungen.size())
    {
      return null;
    }
    return buchungen.elementAt(bi);
  }
}
