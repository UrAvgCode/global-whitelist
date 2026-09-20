# Global Whitelist

**Global Whitelist** is a lightweight Velocity plugin that brings whitelist functionality to your proxy network. It
supports all the standard whitelist commands and uses the same `whitelist.json` format as vanilla Minecraft servers.
This means you can easily migrate your existing whitelists.

### Floodgate Support

Global Whitelist supports Bedrock players through Floodgate.
If Floodgate is installed you can whitelist both Java and Bedrock players with the `platform` argument.
If no platform is specified, it will automatically try to determine whether the player is a Java or Bedrock player based
on their username.

## Commands & Permissions

| Command                                       | Description                                                                     | Permission              |
|-----------------------------------------------|---------------------------------------------------------------------------------|-------------------------|
| `/globalwhitelist help`                       | Display the help section                                                        | `globalwhitelist`       |
| `/globalwhitelist add <player> [platform]`    | Add a player to the whitelist                                                   | `globalwhitelist`       |
| `/globalwhitelist remove <player> [platform]` | Remove a player from the whitelist                                              | `globalwhitelist`       |
| `/globalwhitelist list`                       | Displays all players in the whitelist                                           | `globalwhitelist`       |
| `/globalwhitelist on`                         | Enable the whitelist                                                            | `globalwhitelist.admin` |
| `/globalwhitelist off`                        | Disable the whitelist                                                           | `globalwhitelist.admin` |
| `/globalwhitelist enforced`                   | Enforce the whitelist. Players not on the whitelist (if enabled) will be kicked | `globalwhitelist.admin` |
| `/globalwhitelist unenforced`                 | Disable enforcement. Players not on the whitelist will not be kicked            | `globalwhitelist.admin` |
| `/globalwhitelist reload`                     | Reload the whitelist configuration                                              | `globalwhitelist.admin` |
