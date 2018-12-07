# kela-service

1. Kopioi ~/oph-configuration/common.properties tiedostoon jostain ympäristöstä konfiguraatiot
2. Muuta organisaatio.solr.url, tarjonta.solr.baseurl käyttämään julkiosoitetta.
3. Muuta tietokantojen polut tunneloiduiksi. kela-tarjontadb.url, kela-organisaatiodb.url
4. Käynnistä palvelin StartDev luokasta
5. Tarkista serverin tila: curl -v "http://localhost:8081/kela-service/resources/kela/export?command=STATUS"

