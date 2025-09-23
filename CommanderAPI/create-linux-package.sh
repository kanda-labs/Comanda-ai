#!/bin/bash

# Script para criar pacotes Linux a partir do JAR fat
# Cria estrutura básica para instalação em sistemas Linux

set -e

echo "🐧 CRIANDO PACOTE LINUX - CommanderAPI"
echo "======================================="

# Verificar se o JAR existe
JAR_FILE="build/libs/CommanderAPI-fat.jar"
if [ ! -f "$JAR_FILE" ]; then
    echo "❌ Erro: JAR não encontrado. Execute './gradlew fatJar' primeiro"
    exit 1
fi

# Preparar diretórios
LINUX_DIR="build/linux-package"
rm -rf "$LINUX_DIR"
mkdir -p "$LINUX_DIR/CommanderAPI/bin"
mkdir -p "$LINUX_DIR/CommanderAPI/lib"

echo "📦 Preparando estrutura do pacote Linux..."

# Copiar JAR
cp "$JAR_FILE" "$LINUX_DIR/CommanderAPI/lib/"

# Criar script de execução
cat > "$LINUX_DIR/CommanderAPI/bin/commanderapi" << 'EOF'
#!/bin/bash
# CommanderAPI Launcher Script

# Detectar diretório de instalação
DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && cd .. && pwd )"

# Verificar Java
if ! command -v java &> /dev/null; then
    echo "❌ Java não encontrado. Por favor instale Java 17 ou superior."
    echo "   Ubuntu/Debian: sudo apt install openjdk-17-jre"
    echo "   RedHat/CentOS: sudo yum install java-17-openjdk"
    exit 1
fi

# Verificar versão do Java
JAVA_VERSION=$(java -version 2>&1 | head -n 1 | cut -d'"' -f2 | cut -d'.' -f1)
if [ "$JAVA_VERSION" -lt 17 ]; then
    echo "⚠️ Java $JAVA_VERSION detectado. CommanderAPI requer Java 17 ou superior."
    exit 1
fi

# Executar CommanderAPI
echo "🚀 Iniciando CommanderAPI..."
echo "   API: http://localhost:8081"
echo "   Swagger UI: http://localhost:8081/swagger-ui"
echo ""
exec java -Xmx512m -Dfile.encoding=UTF-8 -jar "$DIR/lib/CommanderAPI-fat.jar" "$@"
EOF

chmod +x "$LINUX_DIR/CommanderAPI/bin/commanderapi"

# Criar arquivo .desktop para integração com desktop Linux
cat > "$LINUX_DIR/CommanderAPI/CommanderAPI.desktop" << EOF
[Desktop Entry]
Name=CommanderAPI
Comment=Restaurant Order Management System
Exec=/opt/CommanderAPI/bin/commanderapi
Terminal=true
Type=Application
Icon=/opt/CommanderAPI/icon.png
Categories=Development;
EOF

# Criar README de instalação
cat > "$LINUX_DIR/CommanderAPI/README.md" << EOF
# CommanderAPI - Linux Installation

## Requisitos
- Java 17 ou superior

## Instalação Manual

### 1. Instalar Java (se necessário)
\`\`\`bash
# Ubuntu/Debian
sudo apt update
sudo apt install openjdk-17-jre

# RedHat/CentOS
sudo yum install java-17-openjdk
\`\`\`

### 2. Instalar CommanderAPI

#### Opção A: Instalação global (requer sudo)
\`\`\`bash
sudo cp -r CommanderAPI /opt/
sudo ln -sf /opt/CommanderAPI/bin/commanderapi /usr/local/bin/commanderapi
\`\`\`

#### Opção B: Instalação local (sem sudo)
\`\`\`bash
cp -r CommanderAPI ~/
echo 'export PATH="\$HOME/CommanderAPI/bin:\$PATH"' >> ~/.bashrc
source ~/.bashrc
\`\`\`

### 3. Executar
\`\`\`bash
commanderapi
\`\`\`

## Desinstalação

### Se instalado globalmente:
\`\`\`bash
sudo rm -rf /opt/CommanderAPI
sudo rm /usr/local/bin/commanderapi
\`\`\`

### Se instalado localmente:
\`\`\`bash
rm -rf ~/CommanderAPI
# Remover linha do PATH do ~/.bashrc manualmente
\`\`\`

## Endpoints
- API: http://localhost:8081
- Swagger UI: http://localhost:8081/swagger-ui
- Health Check: http://localhost:8081/health
EOF

# Criar script de instalação automática
cat > "$LINUX_DIR/install.sh" << 'EOF'
#!/bin/bash

echo "📦 Instalador CommanderAPI para Linux"
echo "====================================="

# Verificar Java
if ! command -v java &> /dev/null; then
    echo "❌ Java não encontrado. Instalando..."
    if command -v apt &> /dev/null; then
        sudo apt update && sudo apt install -y openjdk-17-jre
    elif command -v yum &> /dev/null; then
        sudo yum install -y java-17-openjdk
    else
        echo "Por favor instale Java 17 manualmente"
        exit 1
    fi
fi

# Perguntar tipo de instalação
echo ""
echo "Escolha o tipo de instalação:"
echo "1) Global (em /opt - requer sudo)"
echo "2) Local (em $HOME)"
read -p "Opção [1/2]: " INSTALL_TYPE

if [ "$INSTALL_TYPE" = "1" ]; then
    echo "🔧 Instalando globalmente..."
    sudo cp -r CommanderAPI /opt/
    sudo ln -sf /opt/CommanderAPI/bin/commanderapi /usr/local/bin/commanderapi
    echo "✅ Instalado em /opt/CommanderAPI"
    echo "   Execute: commanderapi"
else
    echo "🔧 Instalando localmente..."
    cp -r CommanderAPI ~/

    # Adicionar ao PATH
    SHELL_RC=""
    if [ -f ~/.bashrc ]; then
        SHELL_RC=~/.bashrc
    elif [ -f ~/.zshrc ]; then
        SHELL_RC=~/.zshrc
    fi

    if [ ! -z "$SHELL_RC" ]; then
        echo 'export PATH="$HOME/CommanderAPI/bin:$PATH"' >> "$SHELL_RC"
        echo "✅ Instalado em ~/CommanderAPI"
        echo "   Execute: source $SHELL_RC && commanderapi"
    else
        echo "✅ Instalado em ~/CommanderAPI"
        echo "   Execute: ~/CommanderAPI/bin/commanderapi"
    fi
fi

echo ""
echo "🎉 Instalação concluída!"
echo "   Para iniciar: commanderapi"
echo "   API: http://localhost:8081"
EOF

chmod +x "$LINUX_DIR/install.sh"

# Criar arquivo TAR.GZ para distribuição
echo "📦 Criando arquivo TAR.GZ..."
cd "$LINUX_DIR"
tar -czf CommanderAPI-linux.tar.gz CommanderAPI/ install.sh
cd - > /dev/null

# Criar DEB básico (estrutura manual)
echo "📦 Criando pacote DEB básico..."
DEB_DIR="$LINUX_DIR/deb-package"
mkdir -p "$DEB_DIR/commanderapi_1.0/DEBIAN"
mkdir -p "$DEB_DIR/commanderapi_1.0/opt"
mkdir -p "$DEB_DIR/commanderapi_1.0/usr/local/bin"

# Copiar arquivos
cp -r "$LINUX_DIR/CommanderAPI" "$DEB_DIR/commanderapi_1.0/opt/"

# Criar link simbólico
cd "$DEB_DIR/commanderapi_1.0/usr/local/bin"
ln -sf /opt/CommanderAPI/bin/commanderapi commanderapi
cd - > /dev/null

# Criar arquivo control
cat > "$DEB_DIR/commanderapi_1.0/DEBIAN/control" << EOF
Package: commanderapi
Version: 1.0
Section: misc
Priority: optional
Architecture: all
Depends: default-jre (>= 2:1.17) | java17-runtime
Maintainer: KandaLabs <support@kandalabs.com>
Description: CommanderAPI Restaurant Order Management System
 A comprehensive REST API for restaurant order management
 including tables, bills, items and payment processing.
EOF

# Criar postinst script
cat > "$DEB_DIR/commanderapi_1.0/DEBIAN/postinst" << EOF
#!/bin/bash
chmod +x /opt/CommanderAPI/bin/commanderapi
echo "CommanderAPI instalado com sucesso!"
echo "Execute: commanderapi"
EOF
chmod 755 "$DEB_DIR/commanderapi_1.0/DEBIAN/postinst"

# Construir DEB (se dpkg-deb estiver disponível)
if command -v dpkg-deb &> /dev/null; then
    cd "$DEB_DIR"
    dpkg-deb --build commanderapi_1.0
    mv commanderapi_1.0.deb ../commanderapi_1.0_all.deb
    cd - > /dev/null
    echo "✅ Pacote DEB criado: $LINUX_DIR/commanderapi_1.0_all.deb"
else
    echo "⚠️ dpkg-deb não encontrado - DEB não criado"
fi

echo ""
echo "==========================================="
echo "📊 PACOTES LINUX CRIADOS"
echo "==========================================="
echo ""
echo "📦 ARQUIVOS DISPONÍVEIS:"
ls -lah "$LINUX_DIR"/*.tar.gz 2>/dev/null | while read line; do
    echo "   $line"
done
ls -lah "$LINUX_DIR"/*.deb 2>/dev/null | while read line; do
    echo "   $line"
done

echo ""
echo "📥 DISTRIBUIÇÃO:"
echo "1. TAR.GZ Universal: $LINUX_DIR/CommanderAPI-linux.tar.gz"
echo "   Instalação: tar -xzf CommanderAPI-linux.tar.gz && ./install.sh"

if [ -f "$LINUX_DIR/commanderapi_1.0_all.deb" ]; then
    echo ""
    echo "2. Pacote DEB: $LINUX_DIR/commanderapi_1.0_all.deb"
    echo "   Instalação: sudo dpkg -i commanderapi_1.0_all.deb"
fi

echo ""
echo "🐧 Pacotes Linux prontos para distribuição!"