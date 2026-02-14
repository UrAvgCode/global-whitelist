# Global Whitelist

**Global Whitelist** is a lightweight Velocity plugin that brings whitelist functionality to your proxy network. It
supports all the standard whitelist commands and uses the same `whitelist.json` format as vanilla Minecraft servers.
This means you can easily migrate your existing whitelists.

## Commands & Permissions

| Command                                      | Description                                                                     | Permission              |
|----------------------------------------------|---------------------------------------------------------------------------------|-------------------------|
| `/globalwhitelist help`                      | Display the help section                                                        | `globalwhitelist`       |
| `/globalwhitelist add <player>`              | Add a player to the whitelist                                                   | `globalwhitelist`       |
| `/globalwhitelist remove <player>`           | Remove a player from the whitelist                                              | `globalwhitelist`       |
| `/globalwhitelist floodgate_add <player>`    | Add a bedrock player to the whitelist                                           | `globalwhitelist`       |
| `/globalwhitelist floodgate_remove <player>` | Remove a bedrock player from the whitelist                                      | `globalwhitelist`       |
| `/globalwhitelist list`                      | Displays all players in the whitelist                                           | `globalwhitelist`       |
| `/globalwhitelist on`                        | Enable the whitelist                                                            | `globalwhitelist.admin` |
| `/globalwhitelist off`                       | Disable the whitelist                                                           | `globalwhitelist.admin` |
| `/globalwhitelist enforced`                  | Enforce the whitelist. Players not on the whitelist (if enabled) will be kicked | `globalwhitelist.admin` |
| `/globalwhitelist unenforced`                | Disable enforcement. Players not on the whitelist will not be kicked            | `globalwhitelist.admin` |
| `/globalwhitelist reload`                    | Reload the whitelist configuration                                              | `globalwhitelist.admin` |
