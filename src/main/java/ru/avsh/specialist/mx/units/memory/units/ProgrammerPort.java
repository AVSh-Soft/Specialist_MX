package ru.avsh.specialist.mx.units.memory.units;

import org.jetbrains.annotations.NotNull;
import ru.avsh.specialist.mx.units.types.MemoryUnit;

import java.util.Objects;

/**
 * Адресуемое устройство "Порт программатора 'Специалист MX' на базе КР580ВВ55А (i8255A)".
 *
 * @author -=AVSh=-
 */
public final class ProgrammerPort implements MemoryUnit {
    private static final int STORAGE_SIZE = 4;

    private final ProgrammableTimer fProgrammableTimer;

    private int fPA;
    private int fPB;
    private int fPC;
    private int fPR;

    /**
     * Конструктор.
     *
     * @param programmableTimer ссылка на объект ProgrammableTimer - "Программируемый таймер КР580ВИ53 (i8253)"
     */
    public ProgrammerPort(@NotNull ProgrammableTimer programmableTimer) {
        fProgrammableTimer = programmableTimer;
        fPR                = 0b1001_1011      ; // начальная инициализация - режим 0, все порты на ввод
    }

    @Override
    public int storageSize() {
        return STORAGE_SIZE;
    }

    @Override
    public int readByte(int address) {
        if ((address  >= 0) && (address < STORAGE_SIZE)) {
            int result = 0;
            switch (address) {
                case 0:
                    if        ((fPR & 0b1101_0000) == 0b1000_0000) { // режим - 0 или 1, порт А - вывод
                        result = fPA;
                    } else if ((fPR & 0b1101_0000) == 0b1001_0000) { // режим - 0 или 1, порт А - ввод
                        result |= fProgrammableTimer.getCounter2Out() ? 1 : 0;
                    }
                    break;
                case 1:
                    if        ((fPR & 0b1000_0010) == 0b1000_0000) { // режим - 0 или 1, порт B - вывод
                        result = fPB;
                    }
                    break;
                case 2:
                    if        ((fPR & 0b1000_0101) == 0b1000_0000) { // режим - 0, порт C3-C0 - вывод
                        result |= fPC & 0x0F;
                    }
                    if        ((fPR & 0b1000_1100) == 0b1000_0000) { // режим - 0, порт C7-C4 - вывод
                        result |= fPC | 0xF0;
                    }
                    break;
                //case 3:
                //    break;~
                default:
                    break;
            }
            return result;
        }
        return -1;
    }

    @Override
    public void writeByte(int address, int value) {
        if ((address >= 0) && (address < STORAGE_SIZE)) {
            switch (address) {
                case 0: // режим - 0 или 1, порт А - вывод
                    if ((fPR & 0b1101_0000) == 0b1000_0000) {
                        fPA = value;
                    }
                    break;
                case 1: // режим - 0 или 1, порт B - вывод
                    if ((fPR & 0b1000_0010) == 0b1000_0000) {
                        fPB = value;
                    }
                    break;
                case 2: // режим - 0, порт C3-C0 - вывод
                    if ((fPR & 0b1000_0101) == 0b1000_0000) {
                        fPC = (fPC & 0xF0) | (value & 0xF);
                    }
                    // режим - 0, порт C7-C4 - вывод
                    if ((fPR & 0b1000_1100) == 0b1000_0000) {
                        fPC = (fPC & 0xF) | (value & 0xF0);
                    }
                    break;
                case 3:
                    if ((value & 0b1000_0000) == 0) {
                        if ((value & 1) != 0) {
                            fPC |=  1 << ((value >> 1) & 0b111);
                        } else {
                            fPC &= (1 << ((value >> 1) & 0b111)) ^ 0xFF;
                        }
                    } else {
                        fPR = value;
                        fPA = fPB = fPC = 0;
                    }
                    break;
                default:
                    break;
            }
        }
    }

    @Override
    public void reset(boolean clear) {
        fPA = fPB = fPC = 0;
        fPR =   0b1001_1011; // режим 0, все порты на ввод
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ProgrammerPort that = (ProgrammerPort) o;
        return Objects.equals(fProgrammableTimer, that.fProgrammableTimer);
    }

    @Override
    public int hashCode() {
        return Objects.hash(fProgrammableTimer);
    }
}