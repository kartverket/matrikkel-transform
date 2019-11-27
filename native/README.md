# Wrapper rundt SkTrans-transformajonsbiblioteket

## Bakgrunn

Matrikkelen benytter et bibliotek for transformasjoner som er utviklet
hos Kartverket over mange år. Dette biblioteket er skrevet i Fortran
og kan ikke kalles direkte fra Java. Denne wrapperen tilgjengeliggjør
biblioteket for JNI.

## Bygging og installasjon av wrapperen på Windows

Last ned msys2 64bit fra https://www.msys2.org/ (velg msys2-x86_64).

Sjekk at du har fått lastet ned korrekt fil (fra git-bash eller
tilsvarende shell):

	$ echo <SHA256SUM_fra_URLen_over> <nedlastet_filnavn> | sha256sum.exe -c

Følg installasjonsprosedyren beskrevet i URLen over.

I mingw64, installer kompilator og toolchain:

	$ pacman -S gcc make mingw-w64-x86_64-gcc mingw-w64-i686-gcc-libs

(hvis du har behov for å gjøre mer arbeid i msys2 / mingw er pakkene
`git` og `bash-completion` anbefalt.)

Nå skal du forhåpentligvis være klar til å bygge biblioteket, kjør

	$ make install

Du skal da bygge `SosiTransformasjon.dll` og denne skal installeres
til rett sted, som i skrivende stund er `../../all/lib.native64bit`.

Dersom du har satt `JAVA_HOME` i miljøet ditt brukes denne i bygget,
ellers er det en fallback i Makefile. Hvis denne katalogen ikke
eksisterer vil bygget feile.

Du kan bygge en strippet (uten debug-symboler) versjon av
`SosiTransformasjon.dll` ved å kjøre

	$ make stripped

før du installerer.

## Debugging

For å se nærmere på hva SosiTransformasjon.dll gjør kan man sette
miljøvariabelen `SOSITRANSFORM_DEBUG` til 1:

bash:

    $ export SOSITRANSFORM_DEBUG=1
    
cmd:

    > set SOSITRANSFORM_DEBUG=1
    
Denne kan også settes før kjøring av matrikkelen.

## Testing av kode og funksjonalitet

Det er laget flere måter å teste funksjonalitet på, via JNI og
utenom. Ved endringer anbefales det at koden testes gjennom en
minnedebugger, men her er det noen utfordringer:

Biblioteket (og wrapperen) kjøres normalt i kontekst av Java, og
API-funksjonene forutsetter at en JVM sendes med som argument. Det er
ikke trivielt å komme mellom Java og underliggende
native-funksjonalitet for å debugge ev. heap-problemer o.a.

Verktøy som brukes for minnedebugging på Windows integrerer, men ikke
uten videre veldig godt, med gdb. Application Verifier er nødvendig
for heap-debugging, gflags.exe er nyttig for å debugge lasting av
DLLer, Process Monitor kan være til hjelp for til en viss grad å
overvåke hva som foregår underveis i kjøring og Dependency Walker kan
være nyttig til å se avhengigheter til DLLer.

Det er også mulig å debugge i Visual Studio, men det har der vært
problematisk at F-Secure har laget problemer for debuggeren, og policy
i Kartverket har umuliggjort å deaktivere eller avinstallere
dette.

Valgrind har vært til god hjelp, men må kjøres på Linux, så
Windows-spesifikk kode har det ikke vært mulig å teste her.

### Test.java

`Test.java` er en frittstående Java-applikasjon som kan kompileres og
kjøres mot en `SosiTransformasjon.dll` kompilert med

    $ CFLAGS=-DTEST=1 make

eksempel på kjøring er

    $ java Test -l <path til SosiTransformasjon.dll> -i <path til transformation_init-katalog> -x <X-koordinat> -y <Y-koordinat> -f <fra SOSI> -t <til SOSI>

Pather er absolutte og oppgis i Windows-format (med \ og ikke /).

### sandbox.c

Frittstående applikasjon som kaller API-funksjoner i
`SosiTransformasjon.dll`. Denne går utenom JNI-APIet da det har vist
seg vanskelig å laste `jvm.dll` i gdb. `sandbox-jvm.c` er en versjon
som forsøker å kalle funksjonaliten via JNI, men trenger å arbeides
videre på dersom dette skal fungere.

Kompiler applikasjonen med

    $ make -f Makefile.sandbox

og kjør denne med

    $ ./sandbox.exe <path til SosiTransformasjon.dll> <path til transformation_init-katalog> <fra SOSI> <X-koordinat> <Y-koordinat> <til SOSI>

Pather er fremdeles absolutte og oppgis også her i Windows-format (med
\ og ikke /).

Denne kan også kjøres på Linux, da oppgis en vilkårlig streng som path
til `SosiTransformasjon.dll`, og den kan da kjøres i `valgrind`:

    $ valgrind --leak-check=full ./sandbox foo /path/til/transformation_init <fra SOSI> <X-koordinat> <Y-koordinat> <til SOSI>

På denne måten får man altså ikke testet JNI-funksjonaliteten eller
Windows-native kode, og denne bør derfor etterstrebes å holdes til et
minimum.
