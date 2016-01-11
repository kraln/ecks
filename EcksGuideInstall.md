# Section 4: Installing Ecks #

First, you'll want to extract the files from the distributable somewhere you can work with them. Make sure you have all the files listed in [Section 2](EcksGuideGettingStarted.md).

Rename example\_ecks.xml to ecks.xml. Open this up with your favorite text editor, because we're going to be making some changes:

> Unless you're having extreme problems, you'll probably want to change the debugging level to D\_INFO or D\_SUMMARY.

> You'll want to specify a filename for the Ecks log.

> Make sure that the uplink protocol corresponds with the ircd you've chosen.

Most of the rest of this file is self explainatory. You will probably also want to customize register.txt

From a terminal (or command prompt), cd to the directory where you extracted Ecks, and issue the following command:

` java -jar Ecks.jar `

If you see an error related to a missing srvdb.xml, this is normal. Make sure everything links up and works properly, then you'll want to run Ecks in a way that doesn't take control of your terminal. On linux and freebsd, I recommend screen.

[<- Previous](EcksGuideIRCDGuide.md) | [Next ->](EcksGuideFirstLinking.md)























