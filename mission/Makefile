# Diretorias
SRC_DIR = src
BIN_DIR = bin
LIB_DIR = lib
RES_DIR = resources

# Classpath com todas as libs e recursos
CLASSPATH = $(BIN_DIR):$(RES_DIR):$(LIB_DIR)/*

# Classes principais
SERVER_MAIN = core.NaveMae
ROVER_MAIN = core.Rover

# .java
SOURCES := $(shell find $(SRC_DIR) -name "*.java")

# Compilar
all: compile

compile:
	@echo ">> Compilando projeto..."
	mkdir -p $(BIN_DIR)
	javac -cp "$(LIB_DIR)/*" -d $(BIN_DIR) $(SOURCES)
	@echo ">> Compilação concluída!"


# Executar NaveMae
# Uso: make navemae IP=127.0.0.1
navemae:
ifeq ($(IP),)
	$(error Necessário fornecer IP: make navemae IP=<ip>)
endif
	@echo ">> Iniciando NaveMae com IP $(IP)..."
	java -cp "$(CLASSPATH)" $(SERVER_MAIN) $(IP)

# Executar Rover
# Uso: make rover ID=R1 IP_ROVER=127.0.0.2 PORTA=7000 IP_NAVEMAE=127.0.0.1
rover:
ifneq ($(ID),)
ifneq ($(IP_ROVER),)
ifneq ($(PORTA),)
ifneq ($(IP_NAVEMAE),)
	@echo ">> Iniciando Rover $(ID)..."
	java -cp "$(CLASSPATH)" $(ROVER_MAIN) $(ID) $(IP_ROVER) $(PORTA) $(IP_NAVEMAE)
else
	$(error Necessário fornecer IP_NAVEMAE: make rover ID=<id> IP_ROVER=<ip> PORTA=<porta> IP_NAVEMAE=<ip>)
endif
else
	$(error Necessário fornecer PORTA: make rover ID=<id> IP_ROVER=<ip> PORTA=<porta> IP_NAVEMAE=<ip>)
endif
else
	$(error Necessário fornecer IP_ROVER: make rover ID=<id> IP_ROVER=<ip> PORTA=<porta> IP_NAVEMAE=<ip>)
endif
else
	$(error Necessário fornecer ID: make rover ID=<id> IP_ROVER=<ip> PORTA=<porta> IP_NAVEMAE=<ip>)
endif

# Executar GroundControl
# Uso: make groundcontrol 
groundcontrol:
	@echo ">> Iniciando GroundControl com URL $(URL)..."
	java -cp "$(CLASSPATH)" ground_control.GroundControl $(URL)


# Clean
clean:
	@echo ">> A limpar ficheiros compilados..."
	rm -rf $(BIN_DIR)/*
	
# Rebuild
rebuild: clean compile