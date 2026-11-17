# Guia de Instalação e Configuração no iPhone (iOS)

Este guia explica como compilar e instalar o aplicativo **Meu Check-in** no seu iPhone diretamente a partir do Windows, utilizando ferramentas gratuitas e seguras.

---

## 1. Como Obter o Arquivo `.ipa` (Compilação na Nuvem Gratuita)

Como o desenvolvimento iOS exige o Xcode (que roda em macOS) e seu computador utiliza Windows, o projeto já está equipado com uma esteira automatizada no **GitHub Actions** que compila o app na nuvem da Apple gratuitamente.

### Passo a passo para gerar o IPA:
1. Suba esta pasta do projeto para um repositório seu no GitHub (pode ser **Privado**):
   ```bash
   git init
   git add .
   git commit -m "Meu Check-in Android & iOS"
   git branch -M main
   git remote add origin https://github.com/SEU_USUARIO/meu-checkin.git
   git push -u origin main
   ```
2. Acesse seu repositório no navegador e clique na aba **"Actions"**.
3. Selecione a action **"Build iOS (IPA)"** e clique no botão **"Run workflow"** (ou ela rodará automaticamente com o push).
4. Ao finalizar a compilação (~2 a 3 minutos), clique na execução concluída e baixe o artefato:
   - **`meu-checkin-ios-ipa.zip`** (ao descompactar, você terá o arquivo `meu-checkin.ipa`).

---

## 2. Como Instalar o `.ipa` no seu iPhone a partir do Windows

Utilizaremos o **Sideloadly**, que é a ferramenta mais rápida, moderna e segura para instalar arquivos IPA no iPhone pelo Windows.

### Passo a passo:
1. Baixe e instale o **Sideloadly** no Windows (gratuito e oficial):
   - Site: [sideloadly.io](https://sideloadly.io/)
2. Conecte seu iPhone ao computador Windows via cabo USB.
   - *(Se o iPhone perguntar "Confiar neste computador?", toque em "Confiar" e digite o código de desbloqueio do iPhone).*
3. Abra o **Sideloadly**:
   - Seu iPhone aparecerá selecionado no campo **Connected Device**.
   - No campo **Apple account**, digite o seu e-mail do seu Apple ID pessoal (não precisa de conta paga de desenvolvedor).
4. Arraste o arquivo **`meu-checkin.ipa`** para dentro da janela do Sideloadly.
5. Clique no botão **"Start"**:
   - O Sideloadly assinará o app com o seu Apple ID e fará a instalação direta no aparelho.
   - Quando aparecer a mensagem `"Done."`, o ícone do **Meu Check-in** estará na tela inicial do seu iPhone!

### Autorizar o App no iPhone (Apenas no primeiro uso):
No iOS, qualquer app instalado por sideload precisa de autorização inicial:
1. No iPhone, abra **Ajustes** > **Geral** > **VPN e Gerenciamento de Dispositivos**.
2. Toque no seu Apple ID (em *App de Desenvolvedor*).
3. Toque em **"Confiar em [seu e-mail]"** e confirme.
4. Pronto! O app abrirá normalmente.

---

## 3. Como Ativar a Automação 100% Pontual no iPhone (Atalhos do iOS)

Ao contrário do Android, o iOS possui regras rígidas de economia de bateria que podem pausar aplicativos em segundo plano. Para garantir que o seu check-in aconteça **no segundo exato** todos os dias sem você precisar abrir o aplicativo, o Meu Check-in possui integração nativa com o app **Atalhos (Apple Shortcuts)**.

### Passo a passo para agendar no iPhone:
1. No iPhone, abra o app nativo **"Atalhos" (Shortcuts)**.
2. Toque na aba **"Automação"** (na barra inferior) e depois no botão **"+"** (Criar Automação Pessoal).
3. Escolha **"Hora do Dia"**:
   - Defina o horário que você quer que o check-in seja verificado (por exemplo: `09:10` ou `20:10`).
   - Em "Repetir", selecione **Semanalmente** e marque os dias do seu treino (ex: Segunda, Quarta, Sexta).
   - Marque a opção: **"Executar Imediatamente"** (para não pedir confirmação na tela).
   - Toque em **Avançar**.
4. Toque em **"Nova Automação em Branco"** > **"Adicionar Ação"**.
5. Na barra de busca, digite `Meu Check-in` ou selecione a ação:
   - **"Fazer Check-in no Treino"**.
6. Toque em **OK**.

**Pronto!** O próprio iOS executará a ação pontualmente nos dias e horários programados, fará a autenticação segura pelo Keychain, enviará o check-in na API e exibirá a notificação de confirmação 🥋.

