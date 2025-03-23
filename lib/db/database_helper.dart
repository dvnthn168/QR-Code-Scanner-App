import 'package:path/path.dart';
import 'package:sqflite/sqflite.dart';

class DatabaseHelper {
  static final DatabaseHelper _instance = DatabaseHelper._internal();
  factory DatabaseHelper() => _instance;
  static Database? _database;

  DatabaseHelper._internal();

  Future<Database> get database async {
    if (_database != null) return _database!;
    _database = await _initDatabase();
    return _database!;
  }

  Future<Database> _initDatabase() async {
    final path = await getDatabasesPath();
    final dbPath = join(path, 'qr_code_scanner.db');

    return await openDatabase(
      dbPath,
      version: 1,
      onCreate: (db, version) async {
        await db.execute('''
          CREATE TABLE scan_history (
            id INTEGER PRIMARY KEY AUTOINCREMENT,
            content TEXT NOT NULL UNIQUE,
            scanned_at TEXT NOT NULL
          )
        ''');
      },
    );
  }

  Future<void> insertScan(String content) async {
    final db = await database;
    await db.insert('scan_history', {
      'content': content,
      'scanned_at': DateTime.now().toIso8601String(),
    }, conflictAlgorithm: ConflictAlgorithm.replace);
  }

  Future<List<Map<String, dynamic>>> getScanHistory() async {
    final db = await database;
    return await db.query('scan_history', orderBy: 'scanned_at DESC');
  }

  Future<void> close() async {
    final db = await database;
    db.close();
  }
}
