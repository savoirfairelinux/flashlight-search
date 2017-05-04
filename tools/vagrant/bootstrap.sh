#!/bin/bash

CURRENT_USER=$(whoami)
OS_NAME=$(lsb_release -a | grep --color=never -i 'Distributor ID' | cut -d ':' -f2 | cut -c2-)
MAVEN_HOME="~/.m2"
MAVEN_SETTINGS="${MAVEN_HOME}/settings.xml"

HOSTS="10.0.0.45 flashlight flashlight.vm www.flashlight.vm"
HOSTS_DATA="10.0.0.35 flashlight-data flashlight-data.vm"

NAS_LOCATION="nas"
NAS_LOCATION_QC="192.168.50.5"


TRANSFERS="/srv/entreprise/liferay/7_0_EE_GA1/liferay-dxp-digital-enterprise-7.0-ga1-20160617092557801.war "
TRANSFERS="${TRANSFERS}:/srv/entreprise/liferay/7_0_EE_GA1/liferay-dxp-digital-enterprise-osgi-7.0-ga1-20160617092557801.zip "
TRANSFERS="${TRANSFERS}:/srv/entreprise/liferay/7_0_EE_GA1/liferay-dxp-digital-enterprise-tomcat-7.0-ga1-20160617092557801.zip "
TRANSFERS="${TRANSFERS}:/srv/entreprise/liferay/patching-tool-dxp/patching-tool-2.0.6.zip "
TRANSFERS="${TRANSFERS}:/srv/entreprise/liferay/patching-tool-dxp/patching-tool-2.0.6.zip.md5 "
TRANSFERS="${TRANSFERS}:/srv/entreprise/liferay/fix-pack/liferay-fix-pack-de-13-7010.zip "
TRANSFERS="${TRANSFERS}:/srv/entreprise/liferay/fix-pack/liferay-fix-pack-de-13-7010.zip.md5 "
TRANSFERS="${TRANSFERS}:/srv/entreprise/liferay/licenses/7_0_EE/2016-06-21-2017-07-21/activation-key-development-DXP-liferaytrialSFL.xml"

INSTALLERS_PATH="installers"
DEPLOY_PATH="deploy"

welcomeDev() {
    echo "Current user is defined as ${CURRENT_USER}. Do you want to use a different one for SSH connections? [y/n]"
    read USE_ANOTHER_USER
    if [[ "y" == "${USE_ANOTHER_USER}"  ]] ; then
        echo "Enter the user you wish to use:"
        read CURRENT_USER
        echo "Thanks ${CURRENT_USER}!"
    fi

}

setNas() {
    echo "Are you in the Quebec office? [y/n]"
    read IN_QC

    if [[ "y" == "${IN_QC}" || "Y" == "${IN_QC}" ]] ; then
        echo "In Quebec. Using IP address for NAS connection."
        NAS_LOCATION=$NAS_LOCATION_QC
    else
        echo "Not in Quebec. Using domain name for NAS connection."
    fi

}

# Make sure that the correct hosts are defined in /etc/hosts
verifyHosts() {
    if grep "${HOSTS}" /etc/hosts >> /dev/null && grep "${HOSTS_DATA}" /etc/hosts >> /dev/null; then
        echo "Hosts okay"
    else
        if grep "flashlight.vm" /etc/hosts >> /dev/null ; then
            echo "You appear to have hosts for the Flashlight machines, but they do not match the project's IPs."
            echo "Please have the following in your /etc/hosts file and remove conflicting information:"
            echo "8<------------"
            echo ${HOSTS_DATA}
            echo ${HOSTS}
            echo "------------>8"
        else
            echo "Entering hosts data inside /etc/hosts... you may have to provide your password"
            echo "# Vagrant - Flashlight" | sudo tee -a /etc/hosts >> /dev/null
            echo "${HOSTS_DATA}" | sudo tee -a /etc/hosts >> /dev/null
            echo "${HOSTS}" | sudo tee -a /etc/hosts >> /dev/null
        fi
    fi
}

# Make sure that Maven is installed and configured
verifyMaven() {
    which mvn &> /dev/null
    if [ $? -eq 0 ]; then
        echo "Maven installed"
    else
        echo "Maven not installed."
        case ${OS_NAME} in
            'Ubuntu')
                echo "Use the following to install Maven:"
                echo "apt-get install maven"
            ;;
            'Fedora')
                echo "Use the following to install Maven:"
                echo "yum install maven"
            ;;
            'Gentoo')
                echo "Use the following to install Maven:"
                echo "emerge -va maven-bin"
            ;;
            'ManjaroLinux')
                echo "Use the following to install Maven:"
                echo "pacman -S maven"
            ;;
            *)
                echo "I don't know how to install stuff on ${OS_NAME}..."
                echo "Use your package manager to install Maven."
            ;;
        esac
    fi
}

downloadFiles() {
    echo "Downloading installers from NAS. You may need to input your LDAP password or SSH key passphrase."

    rsync -hz --progress -e "ssh -l ${CURRENT_USER}" ${NAS_LOCATION}:${TRANSFERS} ${INSTALLERS_PATH}/
    DOWNLOAD_COMPLETE=$?

    if [[ ${DOWNLOAD_COMPLETE} -eq 0 ]] ; then
        echo "Files are downloaded. Verifying checksums. Everything should print out 'OK'."
        cd ${INSTALLERS_PATH}
        find -type 'f' -iname '*.md5' -exec md5sum -c '{}' \;
        cd ..
    else
        echo "Something went wrong during transfer. Verify command output."
    fi
}

welcomeDev
setNas
verifyHosts
verifyMaven
downloadFiles
