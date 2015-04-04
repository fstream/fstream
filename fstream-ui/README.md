fStream - UI 
===

The website based on HOMER design

Setup
---

From this directory:

	gradle setup


Run
---

To run have it update as you edit:

	grunt live

To create and run an expanded production version:

	grunt build
	grunt server

See it at localhost:9000

Build
---

To create production jar for inclusion into `fstream-web`:

	gradle build

Reset
---

To reset the workspace to an initial state, use:

	gradle reset

