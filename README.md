# Priprema-mobilne


1.Kreirati novi projekat pod nazivom Kolokvijum2. Glavnu aktivnost nazvati MainActivity.
2.Unutar MainActivity postaviti TextView, ImageButton, ImageView, Switch i Button jedno ispod drugoga.
3.Unutar TextView-a prikazati lokaciju uređaja koristeći geografsku širinu i dužinu.
4.Klikom na ImageButton pokreće se kamera i korisnik može da slika fotografiju. Nakon snimanja, fotografija se prikazuje u ImageView ispod dugmeta. Svaki put kada se slika zameni, u Toast poruci prikazati očitavanje žiroskopa po X, Y i Z osi.
5.Kreirati model u bazi za postove sa sajta https://app.beeceptor.com/mock-server/dummy-json i podesiti Retrofit za GET zahtev.
6.Kada se prvi put postavi Switch na "on" dobaviti i u bazu upisati prvih 10 postova. Svaki sledeći put ispisati vrednost iz "title" polja prvog posta u bazi u Toast poruci.
7.Klikom na Button obrisati post u bazi koji se nalazi na prvoj poziciji. Ukoliko su svi postovi obrisani, poslati notifikaciju sa tekstom "Nema više postova!"
8.Tekst Button-a predstavlja vrednosti akcelerometra u realnom vremenu.
9.Kada se Switch prebaci na "off", sadržaj iz TextView-a sačuvati u polju "tekst" unutar SharedPreferences i zameniti vrednost sadržaja iz TextView-a sa imenom prvog kontakta iz Contacts aplikacije.
