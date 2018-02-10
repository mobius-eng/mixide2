package mix.vm;

/**
 * This class encapsulates a MIX instruction.
 *
 * @author Andrea G. B. Tettamanzi
 */
class Instruction
{
    protected MixWord A;
    protected int I;
    protected int F;
    protected int C;
    private int M;

    /**
     * Creates a new instance of an instruction by decoding a word.
     */
    public Instruction(MixWord w)
    {
        A = w.subWord(new FieldSpecification(0, 2));
        I = w.getByte(3).intValue();
        F = w.getByte(4).intValue();
        C = w.getByte(5).intValue();
    }

    /**
     * Executes a conditional jump instruction on a register value.
     */
    protected void conditionalJump(MixVM mix, int M, int v) throws Exception
    {
        switch(F)
        {
            case 0: // J<register>N
                if(v<0)
                {
                    mix.setJumpRegister();
                    mix.setLocationPtr(M);
                }
                break;
            case 1: // J<register>Z
                if(v==0)
                {
                    mix.setJumpRegister();
                    mix.setLocationPtr(M);
                }
                break;
            case 2: // J<register>P
                if(v>0)
                {
                    mix.setJumpRegister();
                    mix.setLocationPtr(M);
                }
                break;
            case 3: // J<register>NN
                if(v>=0)
                {
                    mix.setJumpRegister();
                    mix.setLocationPtr(M);
                }
                break;
            case 4: // J<register>NZ
                if(v!=0)
                {
                    mix.setJumpRegister();
                    mix.setLocationPtr(M);
                }
                break;
            case 5: // J<register>NP
                if(v<=0)
                {
                    mix.setJumpRegister();
                    mix.setLocationPtr(M);
                }
                break;
            case 6: // J<register>E
                if(v%2==0)
                {
                    mix.setJumpRegister();
                    mix.setLocationPtr(M);
                }
                break;
            case 7: // J<register>O
                if(v%2!=0)
                {
                    mix.setJumpRegister();
                    mix.setLocationPtr(M);
                }
                break;
            default: // Unknown OPCODE!
                throw new InstructionNotImplementedException(this, "Unknown jump instruction!");
        }
    }

    /**
     * Returns <var>V</var>, the part of the content of memory address
     * <var>M</var> specified by field specification <var>F</var>.
     *
     * @param mix a MIX virtual machine
     * @return the value of the operand
     */
    private int V(MixVM mix)
    {
            return mix.read(M).subWord(new FieldSpecification(F)).intValue();
    }

    /**
     * Executes the instruction on the given MIX virtual machine.
     */
    public void execute(MixVM mix) throws Exception
    {
        M = A.intValue() + mix.getIndexRegister(I).intValue();

        switch(C)
        {
            case 0: // NOP
                mix.tick(1);
                break;
            case 1:
                if(F==6) // FADD
                {
                    mix.tick(4); // Cf. TAOCP, Volume 2, page 224
                    mix.setAccumulator(mix.getAccumulator().doubleValue() + mix.read(M).doubleValue());
                }
                else // ADD
                {
                    mix.tick(2);
                    mix.setAccumulator(mix.getAccumulator().intValue() + V(mix));
                }
                break;
            case 2:
                if(F==6) // FSUB
                {
                    mix.tick(4); // Cf. TAOCP, Volume 2, page 224
                    mix.setAccumulator(mix.getAccumulator().doubleValue() - mix.read(M).doubleValue());
                }
                else // SUB
                {
                    mix.tick(2);
                    mix.setAccumulator(mix.getAccumulator().intValue() - V(mix));
                }
                break;
            case 3:
                if(F==6) // FMUL
                {
                    mix.tick(9); // Cf. TAOCP, Volume 2, page 224
                    mix.setAccumulator(mix.getAccumulator().doubleValue()*mix.read(M).doubleValue());
                }
                else // MUL
                {
                    mix.tick(10);
                    mix.setExtendedAccumulator(mix.getAccumulator().intValue()*(long)V(mix));
                }
                break;
            case 4:
                if(F==6) // FDIV
                {
                    mix.tick(11); // Cf. TAOCP, Volume 2, page 224
                    mix.setAccumulator(mix.getAccumulator().doubleValue()/mix.read(M).doubleValue());
                }
                else // DIV
                {
                    mix.tick(12);
                    int V = V(mix);
                    long dividend = mix.getExtendedAccumulator().longValue();
                    mix.setExtension((int) dividend%V);
                    long quotient = dividend/V;
                    if(quotient<-MixWord.MAX_VALUE || quotient>MixWord.MAX_VALUE)
                        mix.setAccumulator(0x40000000); // to set the OV toggle...
                    else
                        mix.setAccumulator((int) quotient);
                }
                break;
            case 5:
                switch(F)
                {
                    case 0: // NUM
                        mix.tick(10);
                        MixWord w = mix.getExtendedAccumulator();
                        int n = 0;
                        int unit = 1;
                        for(int i = w.size(); i>0; i--)
                        {
                            int b = w.getByte(i).intValue()%10;
                            n += b*unit;
                            unit *= 10;
                        }
                        mix.setAccumulator(n*w.sign());
                            break;
                    case 1: // CHAR
                        mix.tick(10);
                        n = mix.getAccumulator().intValue();
                        String s = String.valueOf(n);
                        if(s.charAt(0)=='-')
                            s = s.substring(1);
                        w = mix.getExtendedAccumulator();
                        for(int i = w.size(); i>0; i--)
                        {
                            int b = 0;
                            int j = s.length() - w.size() + i - 1;
                            if(j>=0)
                                b = s.charAt(j) - 18;
                                w.setByte(i, new MixByte(b));
                        }
                        mix.setExtendedAccumulator(w);
                        break;
                    case 2: // HLT
                        mix.tick(10);
                        mix.halt();
                        break;
                    case 6: // FLOT
                        mix.tick(3); // Cf. TAOCP, Volume 2, page 224
                        mix.setAccumulator((double) mix.getAccumulator().intValue());
                        break;
                    case 7: // FIX
                        mix.tick(3); // Cf. TAOCP, Volume 2, page 224
                        double d = mix.getAccumulator().doubleValue();
                        if(Math.abs(d)<Math.pow(64.0, 5.0))
                            n = (int) Math.round(d);
                        else
                            n = MixWord.MAX_VALUE*(int)Math.signum(d);
                        mix.setAccumulator(n);
                        break;
                    default: // Unknown OPCODE!
                        throw new InstructionNotImplementedException(this, "Unknown special instruction!");
                }
                break;
            case 6: // byte shift operations...
                mix.tick(2);
                if(M<=0) break; // "M must be nonnegative" (page 135).
                switch(F)
                {
                    case 0: // SLA
                            MixWord w = mix.getAccumulator();
                            w.shiftLeft(M);						
                            break;
                    case 1: // SRA
                            w = mix.getAccumulator();
                            w.shiftRight(M);						
                            break;
                    case 2:	// SLAX
                            w = mix.getExtendedAccumulator();
                            w.shiftLeft(M);
                            mix.setExtendedAccumulator(w);						
                            break;
                    case 3: // SRAX
                            w = mix.getExtendedAccumulator();
                            w.shiftRight(M);
                            mix.setExtendedAccumulator(w);						
                            break;
                    case 4: // SLC
                            w = mix.getExtendedAccumulator();
                            w.rotateLeft(M);
                            mix.setExtendedAccumulator(w);						
                            break;
                    case 5: // SRC
                            w = mix.getExtendedAccumulator();
                            w.rotateRight(M);
                            mix.setExtendedAccumulator(w);						
                            break;
                    case 6: // SLB
                            w = mix.getExtendedAccumulator();
                            w.shiftLeftBinary(M);
                            mix.setExtendedAccumulator(w);
                            break;
                    case 7: // SRB
                            w = mix.getExtendedAccumulator();
                            w.shiftRightBinary(M);
                            mix.setExtendedAccumulator(w);
                            break;
                    default: // Unknown OPCODE!
                            throw new InstructionNotImplementedException(this, "Unknown shift instruction!");
                }
                break;
            case 7: // MOVE
                mix.tick(1 + 2*F);
                int dest = mix.getIndexRegister(1).intValue();
                mix.setIndexRegister(1, mix.getIndexRegister(1).intValue() + F);
                for(int i = 0; i<F; i++)
                        mix.write(dest + i, mix.read(M + i));
                break;
            case 8: // LDA
                mix.tick(2);
                mix.setAccumulator(V(mix));
                break;
            case 9:  // LD1
            case 10: // LD2
            case 11: // LD3
            case 12: // LD4
            case 13: // LD5
            case 14: // LD6
                mix.tick(2);
                mix.setIndexRegister(C - 8, V(mix));
                break;
            case 15: // LDX
                mix.tick(2);
                mix.setExtension(V(mix));
                break;
            case 16: // LDAN
                mix.tick(2);
                mix.setAccumulator(-V(mix));
                break;
            case 17: // LD1N
            case 18: // LD2N
            case 19: // LD3N
            case 20: // LD4N
            case 21: // LD5N
            case 22: // LD6N
                mix.tick(2);
                mix.setIndexRegister(C - 16, -V(mix));
                break;
            case 23: // LDXN
                mix.tick(2);
                mix.setExtension(-V(mix));
                break;
            case 24: // STA
                mix.tick(2);
                mix.write(M, new FieldSpecification(F), mix.getAccumulator());
                break;
            case 25: // ST1
            case 26: // ST2
            case 27: // ST3
            case 28: // ST4
            case 29: // ST5
            case 30: // ST6
                mix.tick(2);
                mix.write(M, new FieldSpecification(F), mix.getIndexRegister(C - 24));
                break;
            case 31: // STX
                mix.tick(2);
                mix.write(M, new FieldSpecification(F), mix.getExtension());
                break;
            case 32: // STJ
                mix.tick(2);
                mix.write(M, new FieldSpecification(F), mix.getJumpRegister());
                break;
            case 33: // STZ
                mix.tick(2);
                mix.write(M, new FieldSpecification(F), new MixWord(mix.REGISTER_WIDTH));
                break;
            case 34: // JBUS
                mix.tick(1);
                if(F<mix.N_IO_UNITS)
                {
                    if(mix.unit(F).isBusy())
                    {
                        mix.setJumpRegister();
                        mix.setLocationPtr(M);
                    }
                }
                else
                    throw new InstructionNotImplementedException(this, "Invalid I/O unit!");
                break;
            case 35: // IOC
                mix.tick(1);
                IOUnit u = mix.unit(F);
                switch(F)
                {
                    case 0:
                    case 1:
                    case 2:
                    case 3:
                    case 4:
                    case 5:
                    case 6:
                    case 7: // Magnetic tape:
                        u.seek(M);
                        break;
                    case 8:
                    case 9:
                    case 10:
                    case 11:
                    case 12:
                    case 13:
                    case 14:
                    case 15: // Disk or drum
                        if(M!=0)
                            throw new InstructionNotImplementedException(this, "Improper I/O control!");
                        break;
                    case 18: // Line printer
                        if(M!=0)
                            throw new InstructionNotImplementedException(this, "Improper I/O control!");
                        u.pagebreak();
                        break;
                    case 20: // Paper tape
                        if(M!=0)
                            throw new InstructionNotImplementedException(this, "Improper I/O control!");
                        u.seek(0);
                        break;
                    default:
                        // Not an appropriate usage.
                        throw new InstructionNotImplementedException(this, "Improper I/O control!");
                }
                break;
            case 36: // IN
                mix.tick(1);
                u = mix.unit(F);
                if(u==null)
                    throw new InstructionNotImplementedException(this, "Invalid I/O unit!");
                int blocksize = u.getBlockSize();
                for(int i = 0; i<blocksize; i++)
                    mix.write(M++, u.read());
                break;
            case 37: // OUT
                mix.tick(1);
                u = mix.unit(F);
                if(u==null)
                    throw new InstructionNotImplementedException(this, "Invalid I/O unit!");
                blocksize = u.getBlockSize();
                for(int i = 0; i<blocksize; i++)
                    u.write(mix.read(M++));
                if(F>=16)
                    u.newline();
                break;
            case 38: // JRED
                mix.tick(1);
                if(F<mix.N_IO_UNITS)
                {
                    if(mix.unit(F).isReady())
                    {
                        mix.setJumpRegister();
                        mix.setLocationPtr(M);
                    }
                }
                else
                    throw new InstructionNotImplementedException(this, "Invalid I/O unit!");
                break;
            case 39: // istruzioni di salto
                mix.tick(1);
                switch(F)
                {
                    case 0: // JMP
                        mix.setJumpRegister();
                    case 1: // JSJ
                        mix.setLocationPtr(M);
                        break;
                    case 2: // JOV
                        if(mix.overflow())
                        {
                            mix.setJumpRegister();
                            mix.setLocationPtr(M);
                        }
                        break;
                    case 3: // JNOV
                        if(!mix.overflow())
                        {
                            mix.setJumpRegister();
                            mix.setLocationPtr(M);
                        }
                        break;
                    case 4: // JL
                        if(mix.cmpL())
                        {
                            mix.setJumpRegister();
                            mix.setLocationPtr(M);
                        }
                        break;
                    case 5: // JE
                        if(mix.cmpE())
                        {
                            mix.setJumpRegister();
                            mix.setLocationPtr(M);
                        }
                        break;
                    case 6: // JG
                        if(mix.cmpG())
                        {
                            mix.setJumpRegister();
                            mix.setLocationPtr(M);
                        }
                        break;
                    case 7: // JGE
                        if(mix.cmpGE())
                        {
                            mix.setJumpRegister();
                            mix.setLocationPtr(M);
                        }
                        break;
                    case 8: // JNE
                        if(mix.cmpNE())
                        {
                            mix.setJumpRegister();
                            mix.setLocationPtr(M);
                        }
                        break;
                    case 9: // JLE
                        if(mix.cmpLE())
                        {
                            mix.setJumpRegister();
                            mix.setLocationPtr(M);
                        }
                        break;
                    default: // Unknown OPCODE!
                        throw new InstructionNotImplementedException(this, "Unknown jump instruction!");
                }
                break;
            case 40: // JA<cond>
                mix.tick(1);
                conditionalJump(mix, M, mix.getAccumulator().intValue());
                break;
            case 41: // J1<cond>
            case 42: // J2<cond>
            case 43: // J3<cond>
            case 44: // J4<cond>
            case 45: // J5<cond>
            case 46: // J6<cond>
                mix.tick(1);
                conditionalJump(mix, M, mix.getIndexRegister(C - 40).intValue());
                break;
            case 47: // JX<cond>
                mix.tick(1);
                conditionalJump(mix, M, mix.getExtension().intValue());
                break;
            case 48: // Accumulator address-transfer instructions:
                mix.tick(1);
                switch(F)
                {
                    case 0: // INCA
                        mix.setAccumulator(mix.getAccumulator().intValue() + M);
                        break;
                    case 1: // DECA
                        mix.setAccumulator(mix.getAccumulator().intValue() - M);
                        break;
                    case 2: // ENTA
                        mix.setAccumulator(M);
                        if(M==0)
                            mix.getAccumulator().setSign(A.sign());
                        break;
                    case 3: // ENNA
                        mix.setAccumulator(-M);
                        if(M==0)
                            mix.getAccumulator().setSign(-A.sign());
                        break;
                    default: // Unknown OPCODE!
                        throw new InstructionNotImplementedException(this, "Unknown address transfer instruction!");
                }
                break;
            case 49: // Index address-transfer instructions:
            case 50:
            case 51:
            case 52:
            case 53:
            case 54:
                mix.tick(1);
                switch(F)
                {
                    case 0: // INCi
                        mix.setIndexRegister(C - 48, mix.getIndexRegister(C - 48).intValue() + M);
                        break;
                    case 1: // DECi
                        mix.setIndexRegister(C - 48, mix.getIndexRegister(C - 48).intValue() - M);
                        break;
                    case 2: // ENTi
                        mix.setIndexRegister(C - 48, M);
                        if(M==0)
                            mix.getIndexRegister(C - 48).setSign(A.sign());
                        break;
                    case 3: // ENNi
                        mix.setIndexRegister(C - 48, -M);
                        if(M==0)
                            mix.getIndexRegister(C - 48).setSign(-A.sign());
                        break;
                    default: // Unknown OPCODE!
                        throw new InstructionNotImplementedException(this, "Unknown address transfer instruction!");
                }
                break;
            case 55: // Extension address-transfer instructions:
                mix.tick(1);
                switch(F)
                {
                    case 0: // INCX
                        mix.setExtension(mix.getExtension().intValue() + M);
                        break;
                    case 1: // DECX
                        mix.setExtension(mix.getExtension().intValue() - M);
                        break;
                    case 2: // ENTX
                        mix.setExtension(M);
                        if(M==0)
                            mix.getExtension().setSign(A.sign());
                        break;
                    case 3: // ENNX
                        mix.setExtension(-M);
                        if(M==0)
                            mix.getExtension().setSign(-A.sign());
                        break;
                    default: // Unknown OPCODE!
                        throw new InstructionNotImplementedException(this, "Unknown address transfer instruction!");
                }
                break;
            case 56:
                if(F==6) // FCMP
                {
                    /*  
                        FCMP checks for "approximately equal to" using an EPSILON
                        value from memory location 0 as described in section 4.2.1.C.
                        If you're using FCMP, make sure you don't put any code or
                        non-floating-point data into location 0.
                     */
                    mix.tick(4); // Cf. TAOCP, Volume 2, page 224
                    // Reads the EPSILON to use for approximate comparison il location 0
                    double epsilon = mix.read(0).doubleValue();
                    // Calculate the difference between rA and the operand
                    double diff = mix.getAccumulator().doubleValue() - mix.read(M).doubleValue();
                    // Result of comparison:
                    int comparison = 0;
                    if(diff>epsilon) comparison = 1;
                    if(diff<-epsilon) comparison = -1;
                    mix.compare(comparison);                                    
                }
                else // CMPA
                {
                    mix.tick(2);
                    mix.compare(mix.getAccumulator().intValue() - V(mix));
                }
                break;
            case 57: // CMP1
            case 58: // CMP2
            case 59: // CMP3
            case 60: // CMP4
            case 61: // CMP5
            case 62: // CMP6
                mix.tick(2);
                mix.compare(mix.getIndexRegister(C - 56).intValue() - V(mix));
                break;
            case 63: // CMPX
                mix.tick(2);
                mix.compare(mix.getExtension().intValue() - V(mix));
                break;
            default: // Unknown OPCODE!
                throw new InstructionNotImplementedException(this, "Unknown instruction!");
        }
    }

    /**
     * Returns a string version of the instruction.
     *
     * @return a string version of this instruction
     */
    public String toString()
    {
        return "" + C + " " + A.intValue() + "," + I + "(" + F + ")";
    }
}
