# Same as -> export newVersion=x.x.x
# Stops at the first command failing, rather than tagging a version that could not be set
set -eu

# The release commit is undone at the end, which would discard any uncommitted change along with it
if ! git diff --quiet || ! git diff --cached --quiet; then
    echo "Commit or stash the changes first" >&2
    exit 1
fi

echo "Enter version number (x.x.x): "
read -r newVersion
case "${newVersion}" in
    "" | *[!0-9.]* | .* | *. | *..*)
        echo "Invalid version number: ${newVersion}" >&2
        exit 1
        ;;
esac

export GPG_TTY=$(tty)
mvn versions:set -DnewVersion="${newVersion}" -DgenerateBackupPoms=false
# The dependency the README shows names the version released, checked before anything is tagged
perl -0pi -e 's{(<artifactId>islands-fx</artifactId>\s*<version>)[^<]*(</version>)}{${1}'"${newVersion}"'${2}}' README.md
if ! grep -qF "<version>${newVersion}</version>" README.md; then
    echo "README.md shows no dependency on islands-fx to give the version" >&2
    git checkout pom.xml README.md
    exit 1
fi
# mvn clean deploy -Prelease -> Done by Github actions
# Only the version change goes in the release, not whatever else lies untracked in the working tree
git add pom.xml README.md
git commit -m "Release ${newVersion}"
git tag "${newVersion}"
git push origin "${newVersion}"
git reset HEAD~1
git checkout pom.xml
# The README of master names the version released, unless it did already
if ! git diff --quiet README.md; then
    git commit -m "Name version ${newVersion} in the README" README.md
fi
# Master moves to the next minor version, Maven sorting the snapshot of a version before its release, unless it
# builds a later minor or major already: after a patch release, or while preparing the next major. The version it
# builds is the <version> right under <project>, the only one indented once
masterVersion=$(sed -n 's:^    <version>\(.*\)</version>$:\1:p' pom.xml)
nextVersion=$(awk -v released="${newVersion}" -v current="${masterVersion}" 'BEGIN {
    split(released, r, "."); split(current, c, "[.-]")
    if (c[1] + 0 > r[1] + 0 || c[1] + 0 == r[1] + 0 && c[2] + 0 > r[2] + 0) exit
    printf "%d.%d.0-SNAPSHOT", r[1], r[2] + 1
}')
if [ -n "${nextVersion}" ]; then
    mvn versions:set -DnewVersion="${nextVersion}" -DgenerateBackupPoms=false
    git commit -m "Raise the snapshot past release ${newVersion}" pom.xml
fi
