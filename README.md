# porcupine

Page resource handling for caribou (although it's just
a ring handler and can work with any ring application).

## Usage

**porcupine** is a library to assist with the management of
page resources (javascript files, stylesheets, favicons).  It
allows your code and templates to declare that they require
certain resources, safe in the knowledge that:

* They will be included in the order in which they are added
* No resource will be included twice

### Declaring resources

To declare that you need resources, you can either do it from
your code, using the __add-resource__ function, or within
a template using the __add-resource__ helper:

    (add-resource <type> <path> [opts])

or

    {{add-resource <type> <path> [opts]}}

At the present time, __:javascript__, __:stylesheet__ and
__:icon__ types are supported.  The path can be relative or
absolute, and will be inserted into the template exactly
as you provide it.  You can also specify an optional __options__
map.  At present the only option available is __:location__,
which you can provide any keyword you like.  If you provide
__:header__ or __:footer__, you can use the template
helpers __header-resources__ and __footer-resources__ to
render them where you want them in the template.

### Rendering resources

In your template, you need to indicate where you want the
resources to be injected after rendering is complete.  You can
use a number of helpers to do this, or you can easily build your
own helper if you need custom behaviour.  The provided helpers are
__all-resources__, __javascript-resources__, __stylesheet-resources__,
__footer-resources__, and __header-resources__.  They do pretty much
exactly what you might think.

**NOTE:**  You will need to use the triple-curly-brace format
in your template in order for the unescaped resource tags to be
injected correctly.

For example:

```html
<html>
  <head>
    {{add-resource :javascript "/foo/bar.js"}}
    {{add-resource :javascript "/foo/quux.js"}}
    {{{all-resources}}}
  </head>
  <body>
    ...
    {{add-resource :javascript "/foo/baz.js"}}
  </body>
</html>
```

will result in this:

```html
<html>
  <head>
    <script type="text/javascript" src="/foo/bar.js"></script>
    <script type="text/javascript" src="/foo/quux.js"></script>
    <script type="text/javascript" src="/foo/baz.js"></script>
  </head>
  <body>
    ...
  </body>
</html>

Note that the positions of the calls to __add-resource__ are not relevant,
and that the resources are injected at the place where __{{{all-resources}}}__
appears.

## License

Copyright © 2013 FIXME

Distributed under the Eclipse Public License, the same as Clojure.
