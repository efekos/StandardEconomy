# StandardEconomy

This is a pretty basic economy plugin with standard commands and a basic bank system. Nothing too lightweight, nothing
too fancy, just the thing you need for a new server.

## Commands

Besides essential commands which are /bal,/balance,/money and /pay. This plugin adds a few new commands:

* /economy or /eco - Economy management command with the ability to `add`,`remove`,`change` and `reset` players' balances. Requires `economy.admin`.
* /bank - Only visible if bank system is enabled. Everything about banks.
* /baltop - Top 10 richest people in the server. Refreshes every 10 minutes.

## Permissions

This plugin lets players use all features of it like normal. The only permission is `economy.admin`, required to access
`/eco` and `/economy` commands.

## Configuration

This plugin comes with the following configuration file.

````yaml
enable-banks: false # Enables /bank
digits: 2 # Determines how many fractional digits should be in the number when it is formatted.
currency: # Currency information, used by both other Vault dependants and StandardEconomy.
  symbol: '$'
  single: 'dollar'
  plural: 'dollars'
default-balance: 50
clear-cache-interval: 10 # In minutes, so the default value will reset /baltop cache every 10 minutes.

database:
  # 'true' to use MySQL, 'false' to use SQLite.
  enable-external-databases: false
  host: 'localhost'
  port: '3306'
  username: 'admin'
  password: 'admin'
````

There is also a `lang.yml` file that lets you change all the messages of the plugin. This plugin uses
[MiniMessage](https://docs.advntr.dev/minimessage/format.html) to translate message colors. Most placeholders are
already colored, and it is impossible to change their color, so it is recommended to not play with colors much.

## License

This plugin's source code is public under the MIT License.