# PlayerCompass
Track a player (with permission granted beforehand) with a compass.

### Commands:
- `/compass` - Displays the command help message
- `/compass reset` - Resets compass to vanilla coordinate (default is current world spawn).
- `/compass bed` - Sets compass to point to your bed, provided you have a bed spawn location. Otherwise equivalent of `/compass reset`
- `/compass allow <player>` - Allows the specified player to track them with `/compass track <player>`
- `/compass disallow` - Removes everyone that was allowed to track this player.
- `/compass track <player>` - Sets compass to point to the specified player, if the trackee has allowed the tracker. Compass location updates every 15 seconds. Executes `/compass reset` if the trackee goes offline, or has disallowed the player.

Player must be holding a compass when executing `/compass track` and `/compass bed` commands.
