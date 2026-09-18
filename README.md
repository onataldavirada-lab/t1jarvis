# Ligar PC Voz — versão corrigida

Esta versão remove AndroidX/AppCompat para evitar o erro `Duplicate class kotlin...`
durante o build no GitHub Actions.

Configuração Wake-on-LAN:
- MAC: F4:B5:20:5C:1B:5E
- Broadcast: 192.168.15.255
- Porta: 9

Frases:
- ligar o PC
- ligar PC
- ligar computador
- ligar o computador

Build:
1. Envie o conteúdo para a raiz do repositório.
2. Actions > Build APK > Run workflow.
3. Baixe o artifact `LigarPC-Voz-APK`.
4. Extraia e instale `app-debug.apk`.
