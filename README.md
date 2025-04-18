# AnguillaSearch

A simple command line search engine. 

## Description

AnguillaSearch crawls the  specified net starting from the seed URLs 

https://github.com/user-attachments/assets/591f764a-98f4-403b-8e64-7eb0ec349a96

## Usage

The program supports the following command line arguments:
| Argument | Value | Default | Description |
| -------- | ----- | ------- | ----------- |
| --help   |       |         | Prints help |
| --color  |       | off     | Use ANSI-Colors to highlight searchresults |
| -r       | 0 to 3| 2       | Rank result by: <br> 0 - TF-IDF <br> 1 - Cosine Similarity <br> 2 - Combination of cosine similarity and Pagerank <br> 3 - Cosine Similarity with weights <br>&nbsp;If a specific word is multiple times in the searchquery it will be weighted accordingly.
|  last arg|       | cheesy1 | Provide a path to a JSON-file or seed URLs sperated by " "

The last argument can be a filepath to a json file or seedurls seperated by spaces.
## Example
We want to utilize colors and use ranking method 3. We supply the seed URLs of chessy2 with the last argument:

```java -jar ./target/anguillasearch-1.0.0-SNAPSHOT.jar --color -r 3 "http://shropshireblue24.cheesy2 http://burrata.cheesy2 http://stilton24.cheesy2"```


with the docker container:


    docker run --rm -it \
    --net anguilla-search-dev --ip 172.32.0.8 \
    --dns="172.32.0.2" --dns="8.8.8.8" --dns="4.4.4.4" \
    -u $(id -u):$(id -g) \
    --mount type=bind,source="$(pwd)"/target/libs,target=/opt/anguillasearch/libs,readonly \
    --mount type=bind,source="$(pwd)"/logs,target=/opt/anguillasearch/logs \
    --mount type=bind,source="$(pwd)"/figures,target=/opt/anguillasearch/figures \
    registry.propra-prod1.fernuni-hagen.de/propra/ws24-25/q7062150/anguilla-search:latest \
    --color -r 3 "http://shropshireblue24.cheesy2 http://burrata.cheesy2 http://stilton24.cheesy2"


## Installation
You need the following dependencies:
- Java 17
- Maven
- Git
For Windows:
- Docker Desktop 
- WSL2
For Linux:
- Docker-Engine
- Docker-Compose
- Git
### Option 1 (You have the libraries and the intranet is reachable)
- Download latest release
- Move the program into the folder which contains the libs folder
- Run the program with ``java -jar ./anguillasearch-release.jar``
### Option 2 (You don't have the libraries and the intranet)
- If you are on windows use your WSL2 virtual machine
- Clone the project using ``git clone https://git.propra-prod1.fernuni-hagen.de/propra/ws24-25/q7062150.git``
- Navigate into the cloned repository
- Install libs with command :
  ``mvn install dependency:copy-dependencies -Dmaven.test.skip``
- Start intranet using the following command:
  ``docker compose -f docker-compose-intranet.yml up -d``
- Pull the docker image using the command:
    ``docker login registry.propra-prod1.fernuni-hagen.de``
- Pull the latest image from the registry:
  ``docker image pull registry.propra-prod1.fernuni-hagen.de/propra/ws24-25/q7062150/anguilla-search:latest``
- Create logs folder otherwise it will be created with root privileges and you will get error messages:
  ``mkdir logs ``
- Navigate to the root directory of your project folder and execute the container.
    ```bash
    docker run --rm -it \
    --net anguilla-search-dev --ip 172.32.0.8 \
    --dns="172.32.0.2" --dns="8.8.8.8" --dns="4.4.4.4" \
    -u $(id -u):$(id -g) \
    --mount type=bind,source="$(pwd)"/target/libs,target=/opt/anguillasearch/libs,readonly \
    --mount type=bind,source="$(pwd)"/logs,target=/opt/anguillasearch/logs \
    --mount type=bind,source="$(pwd)"/figures,target=/opt/anguillasearch/figures \
    registry.propra-prod1.fernuni-hagen.de/propra/ws24-25/q7062150/anguilla-search:latest
    ```


# Anguilla Search (Dev)




This is a complete programming environment with minimal dependencies. VSCode is provided as a web application ([Coder Server](https://coder.com)) with all the necessary extensions.
 In addition, a [Devcontainers](https://code.visualstudio.com/docs/devcontainers/containers) configuration is included, which also provides a ready to use environment in VSCode with just a few clicks so that you can start programming immediately.

A template for the Java program to be developed is also included. This contains a preconfigured Maven *pom.xml* file with the required dependencies and reporting tools.

This programming environment is supplied with Java 17 and Maven.

An intranet with various websites is also automatically provided to ensure a static environment for development and testing.

**Features**

- VSCode in the web browser with all required extensions.
- Java 17 and Maven already integrated.
- DevContainers environment with all required extensions for easy working with a locally available VSCode.
- Static intranet for development and testing.


## Contents

- [Anguilla Search (Dev)](#anguilla-search-dev)
  - [Contents](#contents)
  - [Installation (Dependencies)](#installation-dependencies)
  - [Quick Start (Working with VSCode in a Web Browser)](#quick-start-working-with-vscode-in-a-web-browser)
  - [Reach the Intranet from Host](#reach-the-intranet-from-host)
  - [Installation (using local VSCode or VSCodium)](#installation-using-local-vscode-or-vscodium)
    - [Locally Installed VSCode](#locally-installed-vscode)
    - [Locally Installed VSCodium](#locally-installed-vscodium)
  - [Usage (local VSCode or VSCodium)](#usage-local-vscode-or-vscodium)
  - [Generate and View Code Style Reportings](#generate-and-view-code-style-reportings)
  - [Execute the JAR](#execute-the-jar)
    - [Execute the JAR in the Development Environment](#execute-the-jar-in-the-development-environment)
    - [Execute the JAR using the Docker Image](#execute-the-jar-using-the-docker-image)
  - [List of pre-installed VSCode/Codium Extensions](#list-of-pre-installed-vscodecodium-extensions)


## Installation (Dependencies)

At least you need the following dependencies:

- Linux
    - Docker-Engine
    - Docker Compose
    - Git
- Mac
    - Docker Desktop
    - Git
-  Windows
    - Docker Desktop
    - WSL2 (Git is already included)


Clone this git repository

```bash
git clone https://git.propra-prod1.fernuni-hagen.de/propra/ws24-25/q7062150.git \
q7062150.git
```


## Quick Start (Working with VSCode in a Web Browser)

> *NOTE:* On the first start the docker images will be downloaded and the VSCode extensions will be automatically installed.


Navigate into the clone repository

```bash
cd q7062150.git
```

Start the docker environment and the intranet

```bash
docker compose -f docker-compose-dev.yml -f docker-compose-intranet.yml up
```


Open the following url in a web browser: [http://127.0.0.1:8080](http://127.0.0.1:8080)


Start programming :) ...


## Reach the Intranet from Host

To be able to visit the intranet website with your host's web browser, you must add the following DNS server: *172.32.0.2*


## Installation (using local VSCode or VSCodium)

The following steps are not necessary and are meant for experienced users. For the Programmierpraktikum it is sufficient to use VSCode in the browser as explained above. Continue only if you want to use a local installation of VSCode or VSCodium and know what you are doing.


### Locally Installed VSCode

1. Install VSCode. Installation instructions can be found [here](https://code.visualstudio.com/).

2. Install the [devcontainers](https://marketplace.visualstudio.com/items?itemName=ms-vscode-remote.remote-containers) extensions.


### Locally Installed VSCodium

*WARNING:* Devcontainers is a proprietary extension. Therefore, we need a workaround to make it work with VSCodium.

Here the problem is, that the *devcontainers* extension is not able to install the vscode-server inside of the docker container. We therefore use the *Open Remote - SSH* extension to install the vscode-server.


1. Install VSCodium. Installation instructions can be found [here](https://vscodium.com/)

2. Download the following extensions and install the *VSIX* files manually (*Extensions* -> *Install from VSIX...*).
   1. [Devcontainers](https://marketplace.visualstudio.com/items?itemName=ms-vscode-remote.remote-containers)
   2. [Open Remote - SSH](https://open-vsx.org/extension/jeanp413/open-remote-ssh)

3. Start the docker environment
    ```bash
    SSH_SERVER=true CODER_SERVER=false docker compose -f docker-compose-dev.yml up -d
    ```
4. Connect to the development container with VSCodium using the *Open Remote - SSH* extension (this will install the vscode-server):
   1. Open the Command Palette (*Ctrl + Shift + P*)
   2. Execute `> Remote-SSH: Connect to Host...`
   3. Connect to following host (password: vscode). A new window opens automatically.
       ```
       vscode@127.0.0.1:30022
       ```
   4. After connection has established without errors, close the connection (`Close Remote Connection`) and close the VSCodium window.
   5. Stop the docker environment:
       ```bash
       docker compose -f docker-compose-dev.yml down
       ```


## Usage (local VSCode or VSCodium)

1. Open the cloned *q7062150.git* folder with VSCode or VSCodium. Now Code prompt you to *Reopen in Container*. Note that the first start may take some time, as the necessary docker images must be loaded and the extensions installed within the *devcontainers* environment. You can also open the *devcontainers* environment by opening the Command Palette (*Ctrl + Shift + P*) and executing `Dev Containers: Reopen in Container`.

2. To go back to the local environment use the `Reopen Folder Locally` command.


## Generate and View Code Style Reportings


The following reportings can be generated:

- [PMD](https://pmd.github.io/): reports/pmd-report.html
- [Checkstyle](https://checkstyle.org/): reports/checkstyle-report.html
- [Spotbugs](https://spotbugs.github.io/): reports/spotbugs-report.html


Open a Terminal in VSCode or VSCodium (*View* -> *Terminal*) and execute the following command in the */home/vscode/workspace/* folder

```bash
mvn clean site
```

> *NOTE:* If you are using the locally installed VSCode or VSCodium, you must first *Reopen in container* if you have not already done so.

The generated project pages including the *Project Reports* can be found under `q7062150.git/target/site/`.

The GitLab CI/CD pipeline will also generate the *Project Reports* on every commit and provide them as a GitLab Page. 
The link to your report page can be found on the right-hand side of your GitLab project page:

![Screenshot of the GitLab Pages link](./docs/gfx/screenshot_gitlab_page_url.png)



## Execute the JAR

It should be ensured that the JAR is functional. This can be done directly in the development environment and also with the docker container created by the CI/CD pipeline.

### Execute the JAR in the Development Environment

Open a Terminal in VSCode or VSCodium (*View* -> *Terminal*) and execute the following command in the */home/vscode/workspace/* folder to build the JAR.
```bash
mvn clean package
```

> *NOTE:* To skip the test during the build, add `-Dmaven.test.skip`

Afterwards you can execute the jar
```bash
java -jar ./target/anguillasearch-1.0.0-SNAPSHOT.jar
```


### Execute the JAR using the Docker Image

To run the docker image created by the CI/CD pipeline.

> *NOTE:* The following commands must be executed on your host.

1. Make sure that the intranet is available.
    ```bash
    docker ps -a --filter status=running \
    --filter name=dns-server --filter name=web-server \
    --format '{{.Names}}\t{{.Status}}'
    ```
    If not, you can start the intranet with the following command.
    ```bash
    docker compose -f docker-compose-intranet.yml up -d
    ```
2. To pull the docker image from your container registry, you must first log in.
    ```bash
    docker login registry.propra-prod1.fernuni-hagen.de
3. Pull the latest image from the registry.
    ```bash
    docker image pull registry.propra-prod1.fernuni-hagen.de/propra/ws24-25/q7062150/anguilla-search:latest
    ```
4. Navigate to the root directory of your project folder and execute the container.
    ```bash
    docker run --rm -it \
    --net anguilla-search-dev --ip 172.32.0.8 \
    --dns="172.32.0.2" --dns="8.8.8.8" --dns="4.4.4.4" \
    -u $(id -u):$(id -g) \
    --mount type=bind,source="$(pwd)"/target/libs,target=/opt/anguillasearch/libs,readonly \
    --mount type=bind,source="$(pwd)"/logs,target=/opt/anguillasearch/logs \
    --mount type=bind,source="$(pwd)"/figures,target=/opt/anguillasearch/figures \
    registry.propra-prod1.fernuni-hagen.de/propra/ws24-25/q7062150/anguilla-search:latest
    ```
    > *NOTE:* Make sure that the directories to be mounted exist on your host and belong to you. If you are not creating visualizations of the network, you can remove the last *--mount*.

5. Stop the intranet if it was started in step 1.
    ```bash
    docker compose -f docker-compose-intranet.yml stop
    ```


## List of pre-installed VSCode/Codium Extensions

| Extension | Description |
| --------- | ----------- |
| *Extension Pack for Java* | Extension Pack for Java is a collection of popular extensions that can help write, test and debug Java applications in Visual Studio Code. |
| *SonarLint* | Linter to detect & fix coding issues locally in JS/TS, Python, PHP, Java, C, C++, C#, Go, IaC. |
| *Git Graph* | View a Git Graph of your repository, and perform Git actions from the graph. |
| *Red Hat Dependency Analytics* | Provides insights on security vulnerabilities in your application dependencies. |
| *Beautify* | Beautify code in place for VS Code. |
| *PlantUML* | Rich PlantUML support for Visual Studio Code. |
| *Markdown All in One* | All you need to write Markdown. |
| *Markdownlint* | Markdown linting and style checking for Visual Studio Code. |
| *Markdown Preview Github Styling* | Changes VS Code's built-in markdown preview to match Github's style. |
| *Meld Diff* | Use meld (or other tools like WinMerge, Beyond Compare, ...) to compare files, folders, clipboard or git changes from visual studio code directly. |
