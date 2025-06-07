
from flask import Flask, render_template, request, redirect, send_from_directory, session
import os

app = Flask(__name__)
app.secret_key = 'secret'  # pour les sessions
USERS_FILE = 'users.txt'
FILES_FOLDER = 'files'

if not os.path.exists(FILES_FOLDER):
    os.makedirs(FILES_FOLDER)

def check_user(username, password):
    if not os.path.exists(USERS_FILE):
        return False
    with open(USERS_FILE, 'r') as f:
        for line in f:
            u, p = line.strip().split(':')
            if u == username and p == password:
                return True
    return False

@app.route('/', methods=['GET', 'POST'])
def login():
    if request.method == 'POST':
        if check_user(request.form['username'], request.form['password']):
            session['user'] = request.form['username']
            return redirect('/files')
        else:
            return "Identifiants incorrects", 403
    return render_template('login.html')

@app.route('/register', methods=['GET', 'POST'])
def register():
    if request.method == 'POST':
        with open(USERS_FILE, 'a') as f:
            f.write(f"{request.form['username']}:{request.form['password']}\n")
        return redirect('/')
    return render_template('register.html')

@app.route('/files')
def files():
    if 'user' not in session:
        return redirect('/')
    file_list = os.listdir(FILES_FOLDER)
    return render_template('files.html', files=file_list)

@app.route('/download/<filename>')
def download(filename):
    if 'user' not in session:
        return redirect('/')
    return send_from_directory(FILES_FOLDER, filename)

if __name__ == '__main__':
    app.run(host='0.0.0.0', port=8088)
