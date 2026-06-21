import socket
import struct
import os

def upload_file_as_blocks(file_path, starting_block_id, target_ip, target_port, remaining_nodes=""):
    
    file_size = os.path.getsize(file_path)
    print(f"Preparing to shard and upload: {file_path} ({file_size} bytes)")
    
    routing_bytes = remaining_nodes.encode('utf-8')
    routing_length = len(routing_bytes)
    
    # 1. Define our logical block size (2 MB)
    BLOCK_SIZE_LIMIT = 2 * 1024 * 1024 
    
    current_block_id = starting_block_id
    
    # 2. Open the file to read it sequentially
    with open(file_path, 'rb') as f:
        while True:
            # Read exactly 2 MB from the hard drive into Python's RAM
            chunk_data = f.read(BLOCK_SIZE_LIMIT)
            
            # If we hit the end of the file, stop looping
            if not chunk_data:
                break
                
            actual_chunk_size = len(chunk_data)
            
            print(f" -> Uploading Block {current_block_id} | Size: {actual_chunk_size} bytes...")

            # 3. Open a NEW socket for each block (Java expects 1 block per connection)
            with socket.socket(socket.AF_INET, socket.SOCK_STREAM) as s:
                s.connect((target_ip, target_port))
                
                # 4. Pack the header with the CURRENT Block ID and EXACT chunk size
                header = struct.pack(f'>q i {routing_length}s q', 
                                     current_block_id, 
                                     routing_length, 
                                     routing_bytes, 
                                     actual_chunk_size)
                
                # 5. Push the header, then push the 2MB of data
                s.sendall(header)
                s.sendall(chunk_data)
            
            # Increment the Block ID for the next 2 MB slice!
            current_block_id += 1

    print("\nFile successfully sharded and uploaded!")

# --- Run the Test ---
# If you provide a 9 MB file, this will generate Blocks 5005, 5006, 5007, 5008, and 5009
upload_file_as_blocks(
    file_path="sample.pdf",
    starting_block_id=5005,
    target_ip="127.0.0.1",
    target_port=9000
)