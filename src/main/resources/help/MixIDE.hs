<?xml version='1.0' encoding='ISO-8859-1' ?>
<!DOCTYPE helpset
  PUBLIC "-//Sun Microsystems Inc.//DTD JavaHelp HelpSet Version 1.0//EN"
         "http://java.sun.com/products/javahelp/helpset_1_0.dtd">

<?TestTarget this is data for the test target ?>

<!-- Help set per l'applicazione MixIDE
  -- Non c'è bisogno di modificare questo file quando si aggiungono
  -- nuove pagine di help.
  -->
<helpset version="1.0">

  <!-- titolo dell'help set, cioè il titolo del'indice degli argomenti
    -- di più alto livello.
    -->
  <title>MixIDE Help</title>

  <!-- mappe: i file mappa, contraddistinti dall'estensione .jhm,
    -- sono usati per associare gli identificatori interni degli
    -- argomenti con gli URL dei file HTML da mostrare all'utente.
    -- "homeID" è l'identificatore interno di argomento che viene
    -- mostrato per default se l'help viene invocato senza
    -- specificare un identificatore.
    -->
  <maps>
     <homeID>main</homeID>
     <mapref location="MixIDE.jhm"/>
  </maps>

  <!-- Informazione sulle viste: informazioni che descrivono i
    -- navigatori utilizzati nel pannello di sinistra della finestra
    -- di visualizzazione dell'help.
    -->
  <view>
    <name>TOC</name>
    <label>Topics</label>
    <type>javax.help.TOCView</type>
    <data>TOC.xml</data>
  </view>

  <view>
    <name>Index</name>
    <label>Indice</label>
    <type>javax.help.IndexView</type>
    <data>index.xml</data>
  </view>

  <view>
    <name>Search</name>
    <label>Cerca</label>
    <type>javax.help.SearchView</type>
    <data engine="com.sun.java.help.search.DefaultSearchEngine">
      MasterSearchIndex
    </data>
  </view>
  
  <!-- Opzionalmente, si possono specificare dei sotto-help set
    -- che verranno fusi automaticamente con l'help set principale
    -- utilizzando il tag "subhelpset", con un attributo "location",
    -- che contiene l'URL dell'help set da fondere.
    -->
  
</helpset>
