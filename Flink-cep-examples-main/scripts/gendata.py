import csv
import random
import time

# Configurazione del dataset
num_rows = 600  # Numero totale di righe
output_file = "login_attempts.csv"

# IP di tentativi falliti e riusciti
failed_login_ips = ["206.168.34.208", "87.236.176.17", "206.168.34.206", "192.168.1.10", "10.0.0.5"]
successful_login_ips = ["129.16.0.30", "203.0.113.42"]

# Generiamo un insieme di IP unici
all_ips = failed_login_ips + successful_login_ips + [f"192.168.100.{i}" for i in range(10, 30)]
random.shuffle(all_ips)

# Timestamp iniziale
start_timestamp = int(time.time() * 1000)  # Millisecondi

# Lista per memorizzare i dati
data = []
remaining_rows = num_rows

# Selezioniamo un IP specifico per garantire il match
target_ip = "206.168.34.206"
timestamp = start_timestamp

# 1+ tentativi falliti iniziali
data.append([timestamp, target_ip, False])
timestamp -= random.randint(10, 50)  # Millisecondi
remaining_rows -= 1

# 265 tentativi falliti, garantendo almeno uno con IP diverso
temp_failed_ips = [ip for ip in failed_login_ips if ip != target_ip]
for i in range(265):
    ip = target_ip if i != 100 else random.choice(temp_failed_ips)  # Forziamo un cambio di IP
    data.append([timestamp, ip, False])
    timestamp -= random.randint(10, 50)
    remaining_rows -= 1
    if remaining_rows == 0:
        break

# 1+ tentativi riusciti finali
data.append([timestamp, target_ip, True])
timestamp -= random.randint(10, 50)
remaining_rows -= 1

# Riempiamo le righe restanti con dati casuali
while remaining_rows > 0:
    ip = random.choice(all_ips)
    success = random.choice([True, False])
    data.append([timestamp, ip, success])
    timestamp -= random.randint(1000, 5000)  # Simuliamo intervalli realistici
    remaining_rows -= 1

# Riordinare per timestamp decrescente
data.sort(reverse=True, key=lambda x: x[0])

# Scrittura su file CSV
with open(output_file, "w", newline="") as file:
    writer = csv.writer(file)
    writer.writerow(["timestamp", "ip_address", "successful_login"])
    writer.writerows(data)

print(f"✅ Dataset generato con successo: {output_file} ({num_rows} righe)")