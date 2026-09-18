# Ligar PC Voz

App Android para o Redmi Note 11 ficar ouvindo frases específicas e enviar Wake-on-LAN ao PC.

## Frases aceitas

- ligar o PC
- ligar PC
- ligar computador
- ligar o computador

## Wake-on-LAN configurado

- MAC: `F4:B5:20:5C:1B:5E`
- Broadcast: `192.168.15.255`
- Porta: `9`

## Como gerar o APK sem Android Studio

1. Crie/abra um repositório no GitHub.
2. Envie TODO o conteúdo deste projeto para a raiz.
3. Vá em **Actions**.
4. Selecione **Build APK**.
5. Clique em **Run workflow**.
6. Quando terminar, abra a execução.
7. Em **Artifacts**, baixe `LigarPC-Voz-APK`.
8. Extraia o ZIP e instale `app-debug.apk` no Redmi.

## Primeiro uso

1. Abra o app.
2. Toque em **INICIAR ESCUTA**.
3. Permita o microfone.
4. Permita notificações.
5. Uma notificação fixa ficará ativa.
6. Fale `ligar o PC`.

## Xiaomi / Redmi

Para evitar que a MIUI encerre o serviço:

- Configurações > Apps > Ligar PC Voz > Bateria > **Sem restrições**
- Ative **Inicialização automática**, se essa opção estiver disponível.
- Mantenha a permissão de microfone.
- Não use "Economia de bateria" para este app.

Após reiniciar o celular, abra o app e toque em **INICIAR ESCUTA** novamente.

## Observação importante

Esta versão usa o reconhecedor de voz do próprio Android. Dependendo da ROM/serviço de reconhecimento instalado, o reconhecimento pode usar internet ou pacotes de idioma offline. Para escuta 100% offline e ainda mais robusta, seria necessário embutir um modelo local de reconhecimento (por exemplo, Vosk), aumentando bastante o tamanho do APK.
