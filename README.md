# SI3 PARM Project 2018-2019
Membres du groupe : Lydia BARAUKOVA, Sylvain MASIA, Julien MOLINIER, Aldric DUCREUX

***

Pour convertir un fichier texte contenant du code assembler en un fichier texte
contenant code hexadecimal lisible par Logisim, suivez les etapes suivantes :

1. Placer le fichier texte contenant votre code assembleur sans erreurs dans le repertoire "Assembler/data/"
2. Renommer ce fichier en "AssemblerIn"
3. Ouvrir cmd (ou Terminal)
4. Dans cmd (ou Terminal), aller dans le repertoire "Assembler"
5. Compiler le code avec la commande : javac -d build/classes src/main/java/assembler/*.java
6. Lancer le code avec la commande : java -cp build/classes assembler.Main
7. Recuperer le code converti dans le fichier "AssemblerOut" se trouvant dans le repertoire "Assembler/data/"

Remarque 1 : il faut avoir java installe sur votre ordinateur
Remarque 2 : pour que java marche dans cmd, il faut rajouter le chemin absolu vers le repertoire bin de java a path (variable d'environnement)
Remarque 3 : au lieu des etapes 3-6, ouvrir, compiler et lancer le code dans un IDE java (par exemple, IntelliJ IDEA)
