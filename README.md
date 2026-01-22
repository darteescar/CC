# CC (Comunicações por Computador)

Projeto de grupo desenvolvido no âmbito da UC de CC.

O projeto consiste na implementação de protocolos que correrm sobre UDP (MissionLink) e TCP (TelemetryStream) para a comunicação entre _Rovers_ e uma Nave Mãe, numa simulação de missões espaciais.
Pode consultar o [enunciado](TP_CC_2526.pdf) do projeto e o respetivo [relatório](Relatorio_CC.pdf).

## Membros do grupo:

* [darteescar](https://github.com/darteescar)
* [luis7788](https://github.com/luis7788)
* [tiagofigueiredo7](https://github.com/tiagofigueiredo7)

![Interface](background.png)

## Dependências 

Para poder correr as topologias criadas e testar os protocolos é necessário ter o [CORE](https://github.com/eivarin/Dockerized-Coreemu-Template) instalado.
Para o CORE ter acesso ao código fonte, é necessário que o mesmo se encontre na diretoria *volume/*, que é partilhada pelo Docker e pela sua máquina.

## Ambiente de testes
Para testar os protocolos abra no CORE qualquer uma destas três topologias desenvolvidas:

- final-clean.xml
- final-easy.xml
- final-hard.xml

Ponha a topologia a correr. Abra os terminais dos nós dos _Rovers_ que pretende testar, da Nave Mãe e do _Ground Control_. Em cada um dos terminais abertos certifique-se que se encontra na diretoria *mission/* :

```bash
$ cd mission/
```

### Compile

Para compilar o projeto faça:

```bash
$ make compile
```

### Run

Para correr a Nave Mãe faça no seu terminal:

```bash
$ make navemae IP=10.0.4.20
```

Para correr os _Rovers_ faça nos seus terminais:

```bash
$ make rover ID=R-X IP_ROVER=X.X.X.X PORTA=7000 IP_NAVEMAE=10.0.4.20
```

Onde o ID e o IP correspodem aos valores contidos na topologia.

Para correr o _Ground Control_ faça no seu terminal:

```bash
$ make groundcontrol
```
### Rebuild

Para reconstruir o projeto faça:

```bash
$ make rebuild
```

### Clean

Para remover os ficheiros gerados pelo compile o projeto faça:

```bash
$ make clean
```
