# Changelog
All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## Unreleased
### Added
- ...

## [2.0.0] - 2019-01-27
### Added
- Character sheet with: abilities, skills, feats, spells
- Ability, race and class pickers
- Skills, feats and spells filters
- Data management: races and classes
### Changed
- Actions on detail page now as small button on top of the page
- Feats can be added to current character

## [1.3.0] - 2019-01-16
### Added
- Support back to SDK 19 (Kitkat - Android 4.4)
### Fixed
- Up button in settings doesn't bring the user back to welcome screen (sdk <= 21)
- More/less button in item details view doesn't work (sdk <= 21)

## [1.2.1] - 2019-01-15 
### Fixed
- Spell filters don't work if only class and/or school selected (without level)
### Added
- Unit tests for util classes (96% code coverage)

## [1.2.0] - 2019-01-14
### Added
- Sources as preferences
- Source in item detail
- Source filtering for feats and spells
- Data migration for favorites
- Preference to disable disclaimer on welcome page
### Changed
- More information on welcome page
### Fixed
- Duplicated class (Rôd & Rod)

## [1.1.0] - 2019-01-12 
### Changed
- Number of items in title (header) on list page
### Added
- Filters for spells based on school, class and level
- Setting for showing long names in lists. List of spells shows then levels.
- Icons for items

## [1.0.0] - 2019-01-03
### Added
- Welcome page
- Navigation
- Data import from Yaml files (spells, skills, feats)
- List pages (spells, skills, feats) with search
- Detail page with toggle (metadata) and link to source (www.pathfinderfr.org)
- Favorites (add/remove, list)
- Preference menu with "Always show details" option
