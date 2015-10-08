### What's new in 2.4.1? ###
  * Fixed: [Issue 63](https://code.google.com/p/eclipse-icons-editor/issues/detail?id=63): Exception for content types

### What's new in 2.4.0? ###
  * Enhancement: [Issue 58](https://code.google.com/p/eclipse-icons-editor/issues/detail?id=58): Scrolling in icon editor
  * Enhancement: Property page with image data (Right click -> Properties)
  * Enhancement: Image content type
  * Enhancement: Fit to screen zoom functionality
  * Enhancement: initial implementation of adjust initial zoom to image size

### What's new in 2.3.6? ###
  * Enhancement: [Issue 55](https://code.google.com/p/eclipse-icons-editor/issues/detail?id=55): Move pixel selections with the mouse, not only keyboard (first implementation in production)
  * Fixed: [Issue 56](https://code.google.com/p/eclipse-icons-editor/issues/detail?id=56): Specific icon. No more palette colors into 2.3.5 version but ok for 2.3.4

### What's new in 2.3.5? ###
  * Fixed: [Issue 46](https://code.google.com/p/eclipse-icons-editor/issues/detail?id=46): Transparency issue with copy/paste
  * Fixed: [Issue 52](https://code.google.com/p/eclipse-icons-editor/issues/detail?id=52): Undo/Redo issue after applying zoom
  * Enhancement: [Issue 51](https://code.google.com/p/eclipse-icons-editor/issues/detail?id=51): Resize Icons bigger than 16x16

### What's new in 2.3.4? ###
  * Fixed: [Issue 48](https://code.google.com/p/eclipse-icons-editor/issues/detail?id=48): Big images. No more handles exception and Workbench closes
  * Performance. Pixel information uses RGB instead of Color
  * Fixed: Exception when closing the editor and no modifications performed in the image.
  * Added jpg and jpeg as editor related extensions.

### What's new in 2.3.3? ###
Icon Editor improvements
  * Copy/Paste/Cut. [Issue 34](https://code.google.com/p/eclipse-icons-editor/issues/detail?id=34). Compatiblity with external editors. Use keyboard shortcuts to perform operations. [Issue 46](https://code.google.com/p/eclipse-icons-editor/issues/detail?id=46) was created to solve a problem with Transparency and copy/paste. Transparent pixels lost transparency information. This functionality was delivered because it is useful even with this defect.

### What's new in 2.3.2? ###
Icon Editor improvements
  * Undo/Redo : [Issue 41](https://code.google.com/p/eclipse-icons-editor/issues/detail?id=41)
  * Crawl WhitebackgroundIcons. Added multi line selection and discard button.

### What's new in 2.3.1? ###
Icon editor fix
  * Fix Alpha Compositing (Blending) algorithm : [Issue 45](https://code.google.com/p/eclipse-icons-editor/issues/detail?id=45)

### What's new in 2.3.0? ###
Icon editor improvements
  * Double buffering : [Issue 33](https://code.google.com/p/eclipse-icons-editor/issues/detail?id=33): Canvas blinking while rectangle selection
  * Cross cursor in the canvas instead of arrow cursor
  * Select and move a pixels block: [Issue 37](https://code.google.com/p/eclipse-icons-editor/issues/detail?id=37) and [Issue 31](https://code.google.com/p/eclipse-icons-editor/issues/detail?id=31) (Only keyboard arrows for the moment)
  * Select all : [Issue 43](https://code.google.com/p/eclipse-icons-editor/issues/detail?id=43)
  * Select and remove : [Issue 42](https://code.google.com/p/eclipse-icons-editor/issues/detail?id=42)

### What's new in 2.2.2? ###
  * Icon Editor: [Issue 35](https://code.google.com/p/eclipse-icons-editor/issues/detail?id=35) and [Issue 29](https://code.google.com/p/eclipse-icons-editor/issues/detail?id=29): Select color even in indirect palette icons.
  * Icon Editor: Go to paint action after color selection if color picker action was selected

Bug fixes
  * Icon Editor: [Issue 38](https://code.google.com/p/eclipse-icons-editor/issues/detail?id=38): Save Failed: Unsupported color depth. Corrupted images

### What's new in 2.2.1? ###
  * Question dialog when trying to save read only files
Bug fixes
  * Exceptions while initalizing transparency with gif and png files with direct palette
  * Ignore case in the extension while checking transparency images

### What's new in 2.2.0? ###
  * Crawl: Discover potential White Background icons
  * Icon Editor: Paint tool item selected when editor opening (Previously was erase)
  * Icon Editor: Paint unfilled and filled rectangle (Redraw while selecting to be improved yet)
  * Icon Editor: Erase available in gif and png files with transparency none.

### What's new in 2.1.0? ###
  * Icon Editor: Create New PNG Icon
  * Icon Editor: Select color in direct palette images
  * Icon Manager: Added scaled icon 16x16 with aspect ratio
Bug fixes
  * [Issue 32](https://code.google.com/p/eclipse-icons-editor/issues/detail?id=32): NPE Icons label decorator
  * Decorator: Set icons label decorator false by default until fixing [Issue 24](https://code.google.com/p/eclipse-icons-editor/issues/detail?id=24)
  * Icon Manager: Fixed bug in rotate left
  * Icon Manager: Fixed Imagedata illegal argument exceptions in scaling small icons
  * Refactoring: Image to Imagedata in some methods