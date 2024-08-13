import socket
import select

def server_connection(dst_ip, dst_port):
    sock = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
    sock.connect((dst_ip, dst_port))
    return sock

BUFFER_SIZE = 140

dst_ip = 'nginx'
dst_port = 4430
srv_port = 443
sockets = []

srv = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
srv.setsockopt(socket.SOL_SOCKET, socket.SO_REUSEADDR, 1)

srv.bind(("0.0.0.0", srv_port))
srv.listen(3)

print(f'listening connection on port {srv_port}')

sockets.append(srv)

while True:
    socks,_,_=select.select(sockets,[],[])

    for sock in socks:
        try:
            if sock==srv:
                client,_=srv.accept()
                print(f"new connection")
                sockets.append(client)
                server=server_connection(dst_ip, dst_port)
                sockets.append(server)
            else:
                buf=sock.recv(BUFFER_SIZE)
                if b'youtube' in buf.lower():
                    print('BAN!!!')
                    continue
                id=sockets.index(sock)
                if id%2==1: 
                    if len(buf) == 0:
                        sockets[id].close()
                        sockets[id+1].close()
                        del sockets[id]
                        del sockets[id]
                    else:
                        sockets[id+1].sendall(buf)
                else: 
                    if len(buf) == 0:
                        sockets[id-1].close()
                        sockets[id].close()
                        del sockets[id-1]
                        del sockets[id-1]
                    else:
                        sockets[id-1].sendall(buf)
        except Exception as e:
            print(e)